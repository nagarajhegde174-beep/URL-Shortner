package com.urlshortener.redirect.controller;

import com.urlshortener.analytics.dto.ClickEventDto;
import com.urlshortener.common.exception.ResourceGoneException;
import com.urlshortener.common.exception.ResourceNotFoundException;
import com.urlshortener.kafka.producer.ClickEventProducer;
import com.urlshortener.redis.service.RedisCacheService;
import com.urlshortener.url.dto.CachedUrl;
import com.urlshortener.url.entity.Link;
import com.urlshortener.url.repository.LinkRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    private final LinkRepository linkRepository;
    private final RedisCacheService redisCacheService;
    private final ClickEventProducer clickEventProducer;
    private final HttpServletRequest request;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        CachedUrl cachedUrl = redisCacheService.getCachedUrl(shortCode);

        if (cachedUrl != null) {
            log.info("Redirect cache HIT for code: {}", shortCode);
            validateRedirect(cachedUrl.isActive(), cachedUrl.getExpiresAt(), shortCode);

            // Fetch link ID from DB to link the click event (since CachedUrl only stores redirect info)
            Link link = linkRepository.findByShortCode(shortCode).orElse(null);
            if (link != null) {
                triggerClickEvent(link.getId());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(cachedUrl.getLongUrl()));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }

        log.info("Redirect cache MISS for code: {}", shortCode);
        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found"));

        validateRedirect(link.isActive(), link.getExpiresAt(), shortCode);

        // Cache only if it is active and not expired
        CachedUrl toCache = CachedUrl.builder()
                .longUrl(link.getLongUrl())
                .isActive(link.isActive())
                .expiresAt(link.getExpiresAt())
                .build();
        redisCacheService.cacheUrl(shortCode, toCache);

        triggerClickEvent(link.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(link.getLongUrl()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private void triggerClickEvent(Long linkId) {
        String ip = getClientIp(request);
        String ipHash = hashIp(ip);
        String referrer = request.getHeader(HttpHeaders.REFERER);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

        ClickEventDto event = ClickEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .linkId(linkId)
                .referrer(referrer)
                .userAgent(userAgent)
                .ipHash(ipHash)
                .clickedAt(Instant.now())
                .build();

        clickEventProducer.publishClickEvent(event);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        String xr = request.getHeader("X-Real-IP");
        if (xr != null && !xr.isEmpty()) {
            return xr.trim();
        }
        return request.getRemoteAddr();
    }

    private String hashIp(String ipAddress) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ipAddress.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private void validateRedirect(boolean isActive, Instant expiresAt, String shortCode) {
        if (!isActive) {
            log.warn("Attempt to access disabled short code: {}", shortCode);
            throw new ResourceNotFoundException("Short URL is disabled");
        }
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            log.warn("Attempt to access expired short code: {}", shortCode);
            throw new ResourceGoneException("Short URL has expired");
        }
    }
}
