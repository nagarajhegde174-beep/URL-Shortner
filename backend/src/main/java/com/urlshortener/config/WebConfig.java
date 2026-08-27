package com.urlshortener.config;

import com.urlshortener.security.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the rate limit interceptor to evaluate all requests
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/**");
    }
}
