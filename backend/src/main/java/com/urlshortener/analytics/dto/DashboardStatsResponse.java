package com.urlshortener.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalLinks;
    private long activeLinks;
    private long totalClicks;
    private boolean isPro;
    private String plan;
    private Instant subscriptionExpiresAt;
}
