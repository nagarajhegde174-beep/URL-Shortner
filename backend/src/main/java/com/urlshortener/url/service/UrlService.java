package com.urlshortener.url.service;

import com.urlshortener.common.exception.*;
import com.urlshortener.config.AppProperties;
import com.urlshortener.redis.service.RedisCacheService;
import com.urlshortener.security.CustomUserDetails;
import com.urlshortener.subscription.model.Subscription;
import com.urlshortener.subscription.model.SubscriptionStatus;
import com.urlshortener.subscription.repository.SubscriptionRepository;
import com.urlshortener.url.dto.CreateUrlRequest;
import com.urlshortener.url.dto.UpdateUrlRequest;
import com.urlshortener.url.dto.UrlResponse;
import com.urlshortener.url.entity.Link;
import com.urlshortener.url.repository.LinkRepository;
import com.urlshortener.user.model.Plan;
import com.urlshortener.user.model.User;
import com.urlshortener.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final Base62Service base62Service;
    private final RedisCacheService redisCacheService;
    private final AppProperties appProperties;

    private static final Set<String> RESERVED_WORDS = Set.of(
            "api", "swagger", "swagger-ui", "v3", "auth", "login", "register", "logout", "refresh", "links"
    );

    @Transactional
    public UrlResponse createUrl(CreateUrlRequest request, CustomUserDetails userDetails) {
        validateLongUrl(request.getLongUrl());
        User user = userDetails.getUser();

        boolean isPro = isUserPro(user);

        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            if (!isPro) {
                throw new SubscriptionExpiredException("Custom short codes are only available to PRO plan users");
            }
            String customCode = request.getCustomCode().trim();
            validateCustomCode(customCode);

            if (linkRepository.existsByShortCode(customCode)) {
                throw new DuplicateShortCodeException("Short code '" + customCode + "' is already in use");
            }

            Link link = Link.builder()
                    .user(user)
                    .shortCode(customCode)
                    .longUrl(request.getLongUrl())
                    .isCustom(true)
                    .isActive(true)
                    .expiresAt(request.getExpiresAt())
                    .build();

            Link savedLink = linkRepository.save(link);
            return mapToResponse(savedLink);
        } else {
            // Free plan URL count check under pessimistic write lock
            if (!isPro) {
                // Lock the user record to serialize requests
                User lockedUser = userRepository.findAndLockById(user.getId())
                        .orElseThrow(() -> new BadRequestException("User not found"));
                long urlCount = linkRepository.countByUser(lockedUser);
                if (urlCount >= 5) {
                    throw new FreePlanLimitException("Free users are limited to 5 short URLs. Upgrade to PRO for unlimited link creation");
                }
            }

            // Generate with unique constraint collision retries (up to 5 attempts)
            for (int attempt = 1; attempt <= 5; attempt++) {
                String code = base62Service.generateShortCode();
                Link link = Link.builder()
                        .user(user)
                        .shortCode(code)
                        .longUrl(request.getLongUrl())
                        .isCustom(false)
                        .isActive(true)
                        .expiresAt(request.getExpiresAt())
                        .build();
                try {
                    // Save inside a sub-transaction block or handle exception directly
                    Link savedLink = linkRepository.saveAndFlush(link);
                    return mapToResponse(savedLink);
                } catch (DataIntegrityViolationException ex) {
                    log.warn("Collision detected on short code '{}' (attempt {}/5)", code, attempt);
                    if (attempt == 5) {
                        throw new ShortCodeGenerationException("Failed to generate a unique short code after 5 attempts");
                    }
                }
            }
            throw new ShortCodeGenerationException("Failed to generate a unique short code");
        }
    }

    @Transactional(readOnly = true)
    public UrlResponse getUrl(Long id, CustomUserDetails userDetails) {
        Link link = linkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Link not found"));

        validateOwnership(link, userDetails);
        return mapToResponse(link);
    }

    @Transactional(readOnly = true)
    public Page<UrlResponse> listUrls(CustomUserDetails userDetails, Pageable pageable) {
        Page<Link> links = linkRepository.findByUser(userDetails.getUser(), pageable);
        return links.map(this::mapToResponse);
    }

    @Transactional
    public UrlResponse updateUrl(Long id, UpdateUrlRequest request, CustomUserDetails userDetails) {
        validateLongUrl(request.getLongUrl());

        Link link = linkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Link not found"));

        validateOwnership(link, userDetails);

        link.setLongUrl(request.getLongUrl());
        link.setExpiresAt(request.getExpiresAt());
        if (request.getIsActive() != null) {
            link.setActive(request.getIsActive());
        }

        Link savedLink = linkRepository.save(link);
        redisCacheService.evictUrl(link.getShortCode());

        return mapToResponse(savedLink);
    }

    @Transactional
    public void deleteUrl(Long id, CustomUserDetails userDetails) {
        Link link = linkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Link not found"));

        validateOwnership(link, userDetails);

        redisCacheService.evictUrl(link.getShortCode());
        linkRepository.delete(link);
    }

    @Transactional
    public UrlResponse toggleUrl(Long id, CustomUserDetails userDetails) {
        Link link = linkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Link not found"));

        validateOwnership(link, userDetails);

        link.setActive(!link.isActive());
        Link savedLink = linkRepository.save(link);
        redisCacheService.evictUrl(link.getShortCode());

        return mapToResponse(savedLink);
    }

    public boolean isUserPro(User user) {
        Subscription subscription = subscriptionRepository.findByUserId(user.getId()).orElse(null);
        if (subscription == null) return false;

        return subscription.getPlan() == Plan.PRO &&
                subscription.getStatus() == SubscriptionStatus.ACTIVE &&
                (subscription.getExpiresAt() == null || subscription.getExpiresAt().isAfter(Instant.now()));
    }

    private void validateLongUrl(String url) {
        try {
            URI uri = new URI(url);
            if (!uri.isAbsolute()) {
                throw new BadRequestException("Invalid URL: must be an absolute URL starting with http:// or https://");
            }
            String scheme = uri.getScheme().toLowerCase();
            if (!scheme.equals("http") && !scheme.equals("https")) {
                throw new BadRequestException("Invalid URL scheme: only http and https are supported");
            }
        } catch (URISyntaxException | NullPointerException e) {
            throw new BadRequestException("Invalid URL format: " + e.getMessage());
        }
    }

    private void validateCustomCode(String code) {
        if (code.length() < 3 || code.length() > 20) {
            throw new InvalidShortCodeException("Custom short code must be between 3 and 20 characters long");
        }
        if (!code.matches("^[a-zA-Z0-9_-]+$")) {
            throw new InvalidShortCodeException("Custom short code can only contain alphanumeric characters, hyphens, and underscores");
        }
        if (RESERVED_WORDS.contains(code.toLowerCase())) {
            throw new InvalidShortCodeException("Custom short code '" + code + "' is a reserved word and cannot be used");
        }
    }

    private void validateOwnership(Link link, CustomUserDetails userDetails) {
        if (link.getUser() == null || !link.getUser().getId().equals(userDetails.getId())) {
            // Throw ResourceNotFoundException to hide the existence of other users' links
            throw new ResourceNotFoundException("Link not found");
        }
    }

    private UrlResponse mapToResponse(Link link) {
        String baseUrl = appProperties.getBaseUrl();
        String fullShortUrl = baseUrl + "/" + link.getShortCode();

        return UrlResponse.builder()
                .id(link.getId())
                .shortCode(link.getShortCode())
                .longUrl(link.getLongUrl())
                .shortUrl(fullShortUrl)
                .isCustom(link.isCustom())
                .isActive(link.isActive())
                .expiresAt(link.getExpiresAt())
                .createdAt(link.getCreatedAt())
                .updatedAt(link.getUpdatedAt())
                .build();
    }
}
