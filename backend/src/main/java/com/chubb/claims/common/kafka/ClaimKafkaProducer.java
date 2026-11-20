package com.chubb.claims.common.kafka;

import com.chubb.claims.common.event.ClaimDomainEvent;
import com.chubb.claims.common.event.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Primary
public class ClaimKafkaProducer implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ClaimKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${chubb.kafka.topics.claim-events:chubb.claims.events}")
    private String topicName;

    public ClaimKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate,
                              ApplicationEventPublisher applicationEventPublisher) {
        this.kafkaTemplate = kafkaTemplate;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(ClaimDomainEvent event) {
        String key = event.getClaimId() != null ? event.getClaimId().toString() : event.getReferenceNumber();
        log.info("Publishing domain event to Kafka topic [{}]: key={}, eventType={}",
                topicName, key, event.getEventType());

        // Also publish locally to Spring application context
        applicationEventPublisher.publishEvent(event);

        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, key, event);
            if (future != null) {
                future.whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka send acknowledged: topic={}, partition={}, offset={}, eventType={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                event.getEventType());
                    } else {
                        log.error("Failed to deliver Kafka event [topic={}, key={}, eventType={}]: {}",
                                topicName, key, event.getEventType(), ex.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error initiating Kafka message send: {}", e.getMessage(), e);
        }
    }
}
