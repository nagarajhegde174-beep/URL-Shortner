package com.urlshortener.redis.service;

import com.urlshortener.url.dto.CachedUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisTemplate<String, CachedUrl> redisTemplate;
    private static final String KEY_PREFIX = "url:";

    public void cacheUrl(String shortCode, CachedUrl cachedUrl) {
        String key = KEY_PREFIX + shortCode;
        Duration ttl = Duration.ofHours(24); // Default 24 hours

        if (cachedUrl.getExpiresAt() != null) {
            Duration timeToExpiry = Duration.between(Instant.now(), cachedUrl.getExpiresAt());
            if (timeToExpiry.isNegative() || timeToExpiry.isZero()) {
                return; // Do not cache already expired links
            }
            ttl = timeToExpiry;
        }
        redisTemplate.opsForValue().set(key, cachedUrl, ttl);
    }

    public CachedUrl getCachedUrl(String shortCode) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + shortCode);
    }

    public void evictUrl(String shortCode) {
        redisTemplate.delete(KEY_PREFIX + shortCode);
    }
}
