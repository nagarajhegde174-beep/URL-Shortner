package com.urlshortener.analytics.controller;

import com.urlshortener.analytics.dto.DashboardStatsResponse;
import com.urlshortener.analytics.dto.LinkAnalyticsResponse;
import com.urlshortener.analytics.service.AnalyticsService;
import com.urlshortener.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getDashboardStats(userDetails));
    }

    @GetMapping("/links/{id}")
    public ResponseEntity<LinkAnalyticsResponse> getLinkAnalytics(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getLinkAnalytics(id, userDetails));
    }
}
