package com.chubb.claims.common.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("!test")
public class KafkaTopicConfig {

    @Value("${chubb.kafka.topics.claim-events:chubb.claims.events}")
    private String claimEventsTopic;

    @Bean
    public NewTopic claimEventsTopic() {
        return TopicBuilder.name(claimEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
