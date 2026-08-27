package com.urlshortener.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String CLICK_TOPIC = "click-events";

    @Bean
    public NewTopic clickEventsTopic() {
        return TopicBuilder.name(CLICK_TOPIC)
                .partitions(3)
                .replicas(1) // 1 replica since we run a single local Kraft broker
                .build();
    }
}
