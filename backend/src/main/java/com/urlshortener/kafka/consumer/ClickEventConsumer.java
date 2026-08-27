package com.urlshortener.kafka.consumer;

import com.urlshortener.analytics.dto.ClickEventDto;
import com.urlshortener.analytics.entity.Click;
import com.urlshortener.analytics.repository.ClickRepository;
import com.urlshortener.analytics.util.UserAgentParser;
import com.urlshortener.config.KafkaConfig;
import com.urlshortener.url.entity.Link;
import com.urlshortener.url.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventConsumer {

    private final ClickRepository clickRepository;
    private final LinkRepository linkRepository;

    @KafkaListener(topics = KafkaConfig.CLICK_TOPIC, groupId = "analytics-group")
    @Transactional
    public void consume(ClickEventDto event, Acknowledgment acknowledgment) {
        log.info("Received ClickEvent for Link ID: {}, Event ID: {}", event.getLinkId(), event.getEventId());

        try {
            Link link = linkRepository.findById(event.getLinkId()).orElse(null);
            if (link == null) {
                log.warn("Link not found for ID: {}. Discarding event.", event.getLinkId());
                acknowledgment.acknowledge();
                return;
            }

            // Duplicate Delivery Check (Idempotency check)
            boolean duplicate = clickRepository.existsByLinkAndIpHashAndClickedAt(
                    link, event.getIpHash(), event.getClickedAt()
            );

            if (duplicate) {
                log.warn("Duplicate ClickEvent detected. Event ID: {}. Skipping database save.", event.getEventId());
            } else {
                String ua = event.getUserAgent();
                Click click = Click.builder()
                        .link(link)
                        .clickedAt(event.getClickedAt())
                        .referrer(event.getReferrer())
                        .userAgent(ua)
                        .deviceType(UserAgentParser.parseDevice(ua))
                        .browser(UserAgentParser.parseBrowser(ua))
                        .operatingSystem(UserAgentParser.parseOS(ua))
                        .ipHash(event.getIpHash())
                        .build();

                clickRepository.save(click);
                log.info("ClickEvent persisted successfully for Link ID: {}, Event ID: {}", event.getLinkId(), event.getEventId());
            }

            // Acknowledge processing success
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing ClickEvent for Link ID: {}. Triggering offset retry. Error: {}", 
                    event.getLinkId(), e.getMessage());
            // Do not call acknowledgment.acknowledge() to trigger retry/replay
            throw e;
        }
    }
}
