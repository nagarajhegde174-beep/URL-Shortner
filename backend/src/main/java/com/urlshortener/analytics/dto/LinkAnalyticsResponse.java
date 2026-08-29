package com.urlshortener.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LinkAnalyticsResponse {
    private Long linkId;
    private String shortCode;
    private String shortUrl;
    private String longUrl;
    private long totalClicks;
    private List<TimeSeriesPoint> clicksOverTime;
    private List<BreakdownItem> deviceBreakdown;
    private List<BreakdownItem> browserBreakdown;
    private List<BreakdownItem> osBreakdown;
    private List<BreakdownItem> topReferrers;

    @Data
    @Builder
    public static class TimeSeriesPoint {
        private String date;
        private long clicks;
    }

    @Data
    @Builder
    public static class BreakdownItem {
        private String label;
        private long count;
    }
}
