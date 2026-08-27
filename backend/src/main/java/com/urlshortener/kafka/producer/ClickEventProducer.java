package com.urlshortener.kafka.producer;

import com.urlshortener.analytics.dto.ClickEventDto;
import com.urlshortener.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventProducer {

    private final KafkaTemplate<String, ClickEventDto> kafkaTemplate;

    public void publishClickEvent(ClickEventDto event) {
        try {
            // Asynchronous send
            kafkaTemplate.send(KafkaConfig.CLICK_TOPIC, event.getLinkId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to deliver Kafka ClickEvent for Link ID: {}. Error: {}", 
                                    event.getLinkId(), ex.getMessage());
                        } else {
                            log.debug("Successfully delivered Kafka ClickEvent for Link ID: {} to partition: {}", 
                                    event.getLinkId(), result.getRecordMetadata().partition());
                        }
                    });
        } catch (Exception e) {
            // Catch and handle failure gracefully to ensure redirect succeeds
            log.error("Kafka temporarily unavailable when publishing ClickEvent for Link ID: {}. Error: {}", 
                    event.getLinkId(), e.getMessage());
        }
    }
}
