package com.urlshortener.redis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisRateLimiterServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private RedisRateLimiterService redisRateLimiterService;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testIsAllowed_Success() {
        // Mock Lua script return 1L (Allowed)
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(1L);

        boolean allowed = redisRateLimiterService.isAllowed("create:user:1", 10, 60);
        assertTrue(allowed);
        verify(redisTemplate).execute(any(RedisScript.class), anyList(), any(Object[].class));
    }

    @Test
    public void testIsAllowed_Exceeded() {
        // Mock Lua script return 0L (Rejected)
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(0L);

        boolean allowed = redisRateLimiterService.isAllowed("create:user:1", 10, 60);
        assertFalse(allowed);
        verify(redisTemplate).execute(any(RedisScript.class), anyList(), any(Object[].class));
    }
}
