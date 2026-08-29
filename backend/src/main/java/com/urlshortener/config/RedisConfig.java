package com.urlshortener.config;

import com.urlshortener.url.dto.CachedUrl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * Typed RedisTemplate for URL caching (CachedUrl values).
     * Used by RedisCacheService.
     */
    @Bean
    public RedisTemplate<String, CachedUrl> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, CachedUrl> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<CachedUrl> serializer = new Jackson2JsonRedisSerializer<>(CachedUrl.class);
        template.setValueSerializer(serializer);
        return template;
    }

    /**
     * General-purpose RedisTemplate<String, Object> for rate limiting.
     * Used by RedisRateLimiterService.
     */
    @Bean
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
