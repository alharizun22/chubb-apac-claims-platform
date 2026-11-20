package com.chubb.claims.common.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

import java.util.Arrays;
import java.util.Map;

/**
 * Early-lifecycle initializer that starts the embedded Kafka broker before
 * property placeholders (such as ${spring.embedded.kafka.brokers}) are resolved.
 */
public class EmbeddedKafkaContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedKafkaContextInitializer.class);
    private static EmbeddedKafkaBroker broker;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        boolean isLocal = Arrays.asList(applicationContext.getEnvironment().getActiveProfiles()).contains("local");
        if (isLocal && broker == null) {
            log.info("Starting Zero-Docker Local Embedded Kafka Broker (1 broker, controlled shutdown, topic: chubb.claims.events)...");
            EmbeddedKafkaZKBroker zkBroker = new EmbeddedKafkaZKBroker(1, true, 1, "chubb.claims.events");
            zkBroker.afterPropertiesSet();
            broker = zkBroker;

            String brokers = zkBroker.getBrokersAsString();
            System.setProperty("spring.embedded.kafka.brokers", brokers);

            applicationContext.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("embeddedKafkaProperties", Map.of(
                            "spring.embedded.kafka.brokers", brokers,
                            "spring.kafka.bootstrap-servers", brokers
                    ))
            );
            log.info("Local Embedded Kafka Broker successfully initialized on: {}", brokers);
        }
    }

    public static EmbeddedKafkaBroker getBroker() {
        return broker;
    }
}
