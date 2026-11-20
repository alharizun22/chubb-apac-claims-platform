package com.chubb.claims.kafka;

import com.chubb.claims.common.event.ClaimSubmittedEvent;
import com.chubb.claims.common.kafka.ClaimKafkaProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private ClaimKafkaProducer producer;

    @BeforeEach
    void setUp() {
        producer = new ClaimKafkaProducer(kafkaTemplate, applicationEventPublisher);
        ReflectionTestUtils.setField(producer, "topicName", "chubb.claims.events");
    }

    @Test
    @DisplayName("Producer sends serialized event to Kafka topic with claimId as key")
    void testProducerSendsEventToKafka() {
        UUID claimId = UUID.randomUUID();
        ClaimSubmittedEvent event = ClaimSubmittedEvent.builder()
                .claimId(claimId)
                .referenceNumber("CHB-SG-2026-0001")
                .market("SG")
                .claimType("MOTOR")
                .claimantId("claimant-1")
                .claimantName("Jane Doe")
                .estimatedAmount(new BigDecimal("3000.00"))
                .build();

        RecordMetadata metadata = new RecordMetadata(new TopicPartition("chubb.claims.events", 0), 0, 0, 0, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(null, metadata);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(eq("chubb.claims.events"), eq(claimId.toString()), any(ClaimSubmittedEvent.class)))
                .thenReturn(future);

        producer.publish(event);

        verify(kafkaTemplate, times(1)).send(eq("chubb.claims.events"), eq(claimId.toString()), eq(event));
        verify(applicationEventPublisher, times(1)).publishEvent(eq(event));
    }
}
