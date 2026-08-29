package com.urlshortener.redis.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Service
public class RedisRateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisRateLimiterService(
            @Qualifier("objectRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String LUA_SCRIPT =
            "local key = KEYS[1]\n" +
            "local now = tonumber(ARGV[1])\n" +
            "local window = tonumber(ARGV[2])\n" +
            "local limit = tonumber(ARGV[3])\n" +
            "local member = ARGV[4]\n" +
            "local clear_before = now - (window * 1000)\n" +
            "redis.call('ZREMRANGEBYSCORE', key, 0, clear_before)\n" +
            "local current_requests = redis.call('ZCARD', key)\n" +
            "if current_requests >= limit then\n" +
            "    return 0\n" +
            "else\n" +
            "    redis.call('ZADD', key, now, member)\n" +
            "    redis.call('EXPIRE', key, window * 2)\n" +
            "    return 1\n" +
            "end";

    public boolean isAllowed(String key, int limit, int windowSeconds) {
        String redisKey = "rate_limit:" + key;
        long nowMs = Instant.now().toEpochMilli();
        String uniqueMember = UUID.randomUUID().toString();

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);

        Long result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(redisKey),
                String.valueOf(nowMs),
                String.valueOf(windowSeconds),
                String.valueOf(limit),
                uniqueMember
        );

        return result != null && result == 1L;
    }
}
