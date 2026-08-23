package com.urlshortener.redirect.controller;

import com.urlshortener.common.exception.ResourceGoneException;
import com.urlshortener.common.exception.ResourceNotFoundException;
import com.urlshortener.redis.service.RedisCacheService;
import com.urlshortener.url.dto.CachedUrl;
import com.urlshortener.url.entity.Link;
import com.urlshortener.url.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    private final LinkRepository linkRepository;
    private final RedisCacheService redisCacheService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        CachedUrl cachedUrl = redisCacheService.getCachedUrl(shortCode);

        if (cachedUrl != null) {
            log.info("Redirect cache HIT for code: {}", shortCode);
            validateRedirect(cachedUrl.isActive(), cachedUrl.getExpiresAt(), shortCode);

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

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(link.getLongUrl()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
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
