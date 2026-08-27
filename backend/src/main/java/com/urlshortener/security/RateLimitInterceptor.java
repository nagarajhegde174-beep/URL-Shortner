package com.urlshortener.security;

import com.urlshortener.common.exception.RateLimitException;
import com.urlshortener.redis.service.RedisRateLimiterService;
import com.urlshortener.url.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisRateLimiterService redisRateLimiterService;
    private final UrlService urlService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 1. Link Creation Rate Limiting (POST /api/links)
        if (uri.equals("/api/links") && method.equalsIgnoreCase(HttpMethod.POST.name())) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                    authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                Long userId = userDetails.getId();
                
                boolean isPro = urlService.isUserPro(userDetails.getUser());
                int limit = isPro ? 100 : 10; // 100/min for PRO, 10/min for FREE
                String key = "create:user:" + userId;

                if (!redisRateLimiterService.isAllowed(key, limit, 60)) {
                    log.warn("Rate limit exceeded for URL creation by User ID: {}", userId);
                    throw new RateLimitException("URL creation limit exceeded. Please wait before creating more links.");
                }
            }
        }

        // 2. Redirect Rate Limiting (GET /{shortCode})
        // Matches GET request with single path variable of length 3-20 characters
        else if (method.equalsIgnoreCase(HttpMethod.GET.name()) && isRedirectPath(uri)) {
            String clientIp = getClientIp(request);
            String hashedIp = hashIp(clientIp);
            String key = "redirect:ip:" + hashedIp;
            int limit = 60; // 60 requests per minute per IP

            if (!redisRateLimiterService.isAllowed(key, limit, 60)) {
                log.warn("Rate limit exceeded for Redirects by IP: {}", hashedIp);
                throw new RateLimitException("Too many redirect requests. Please wait a moment.");
            }
        }

        return true;
    }

    private boolean isRedirectPath(String uri) {
        if (uri == null || uri.equals("/") || uri.startsWith("/api/") || uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs")) {
            return false;
        }
        String path = uri.substring(1); // Remove leading slash
        return path.length() >= 3 && path.length() <= 20 && path.matches("^[a-zA-Z0-9_-]+$");
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
}
