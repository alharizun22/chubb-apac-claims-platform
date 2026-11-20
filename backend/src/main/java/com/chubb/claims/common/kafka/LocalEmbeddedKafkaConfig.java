package com.chubb.claims.common.kafka;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

/**
 * Zero-Docker configuration for local development and verification.
 * Automatically manages the in-process Embedded Kafka broker bean (1 broker, controlled shutdown,
 * topic: 'chubb.claims.events') when running with the 'local' profile.
 */
@Configuration
@Profile("local")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LocalEmbeddedKafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(LocalEmbeddedKafkaConfig.class);

    private EmbeddedKafkaBroker broker;

    @Bean
    public EmbeddedKafkaBroker embeddedKafkaBroker() {
        EmbeddedKafkaBroker existing = EmbeddedKafkaContextInitializer.getBroker();
        if (existing != null) {
            this.broker = existing;
            return existing;
        }

        log.info("Starting Zero-Docker Local Embedded Kafka Broker (1 broker, controlled shutdown)...");
        EmbeddedKafkaZKBroker zkBroker = new EmbeddedKafkaZKBroker(1, true, 1, "chubb.claims.events");
        zkBroker.afterPropertiesSet();
        this.broker = zkBroker;
        String brokerList = zkBroker.getBrokersAsString();
        System.setProperty("spring.embedded.kafka.brokers", brokerList);
        log.info("Local Embedded Kafka Broker started successfully on [{}]", brokerList);
        return zkBroker;
    }

    @PreDestroy
    public void shutdown() {
        if (this.broker != null) {
            log.info("Shutting down Local Embedded Kafka Broker (controlled shutdown)...");
            this.broker.destroy();
        }
    }
}
