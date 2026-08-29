package com.urlshortener.analytics.service;

import com.urlshortener.analytics.dto.DashboardStatsResponse;
import com.urlshortener.analytics.dto.LinkAnalyticsResponse;
import com.urlshortener.analytics.repository.ClickRepository;
import com.urlshortener.common.exception.ResourceNotFoundException;
import com.urlshortener.config.AppProperties;
import com.urlshortener.security.CustomUserDetails;
import com.urlshortener.subscription.model.Subscription;
import com.urlshortener.subscription.repository.SubscriptionRepository;
import com.urlshortener.url.entity.Link;
import com.urlshortener.url.repository.LinkRepository;
import com.urlshortener.url.service.UrlService;
import com.urlshortener.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final LinkRepository linkRepository;
    private final ClickRepository clickRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UrlService urlService;
    private final AppProperties appProperties;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        boolean isPro = urlService.isUserPro(user);
        Subscription subscription = subscriptionRepository.findByUserId(user.getId()).orElse(null);

        return DashboardStatsResponse.builder()
                .totalLinks(linkRepository.countByUser(user))
                .activeLinks(linkRepository.countByUserAndIsActiveTrue(user))
                .totalClicks(clickRepository.countByUserId(user.getId()))
                .isPro(isPro)
                .plan(isPro ? "PRO" : "FREE")
                .subscriptionExpiresAt(subscription != null ? subscription.getExpiresAt() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public LinkAnalyticsResponse getLinkAnalytics(Long linkId, CustomUserDetails userDetails) {
        Link link = linkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Link not found"));

        if (link.getUser() == null || !link.getUser().getId().equals(userDetails.getId())) {
            throw new ResourceNotFoundException("Link not found");
        }

        String baseUrl = appProperties.getBaseUrl();

        return LinkAnalyticsResponse.builder()
                .linkId(link.getId())
                .shortCode(link.getShortCode())
                .shortUrl(baseUrl + "/" + link.getShortCode())
                .longUrl(link.getLongUrl())
                .totalClicks(clickRepository.countByLinkId(link.getId()))
                .clicksOverTime(mapTimeSeries(clickRepository.countClicksByDate(link.getId())))
                .deviceBreakdown(mapBreakdown(clickRepository.countByDeviceType(link.getId())))
                .browserBreakdown(mapBreakdown(clickRepository.countByBrowser(link.getId())))
                .osBreakdown(mapBreakdown(clickRepository.countByOperatingSystem(link.getId())))
                .topReferrers(mapBreakdown(clickRepository.countByReferrer(link.getId())))
                .build();
    }

    private List<LinkAnalyticsResponse.TimeSeriesPoint> mapTimeSeries(List<Object[]> rows) {
        return rows.stream()
                .map(row -> LinkAnalyticsResponse.TimeSeriesPoint.builder()
                        .date(row[0] instanceof Date sqlDate
                                ? sqlDate.toLocalDate().toString()
                                : String.valueOf(row[0]))
                        .clicks(((Number) row[1]).longValue())
                        .build())
                .toList();
    }

    private List<LinkAnalyticsResponse.BreakdownItem> mapBreakdown(List<Object[]> rows) {
        return rows.stream()
                .map(row -> LinkAnalyticsResponse.BreakdownItem.builder()
                        .label(String.valueOf(row[0]))
                        .count(((Number) row[1]).longValue())
                        .build())
                .toList();
    }
}
