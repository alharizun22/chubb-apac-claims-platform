package com.chubb.claims.config;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@Profile("test")
public class TestKafkaConfig {

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("chubb.claims.events.test", 0), 0, 0, 0, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(null, metadata);
        when(template.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));
        when(template.send(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));
        return template;
    }
}
