package com.chubb.claims.exposure.consumer;

import com.chubb.claims.claims.model.Market;
import com.chubb.claims.common.event.ClaimDomainEvent;
import com.chubb.claims.exposure.model.MarketExposure;
import com.chubb.claims.exposure.service.ExposureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Real-time read projection updater.
 * Asynchronously recalculates and updates the market_exposure projection
 * upon receiving claim domain events from Kafka or local domain dispatch.
 */
@Component
public class MarketExposureKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(MarketExposureKafkaConsumer.class);

    private final ExposureService exposureService;
    private final AtomicLong processedEventCount = new AtomicLong(0);

    public MarketExposureKafkaConsumer(ExposureService exposureService) {
        this.exposureService = exposureService;
    }

    @KafkaListener(
            topics = "${chubb.kafka.topics.claim-events:chubb.claims.events}",
            groupId = "chubb-exposure-projector-group",
            autoStartup = "${spring.kafka.listener.auto-startup:true}"
    )
    @EventListener
    @Transactional
    public void onClaimEvent(Object eventPayload) {
        if (eventPayload instanceof ClaimDomainEvent domainEvent) {
            String marketStr = domainEvent.getMarket();
            log.info("[Kafka Exposure Projector] Processing event '{}' for market '{}', claim={}",
                    domainEvent.getEventType(), marketStr, domainEvent.getReferenceNumber());

            if (marketStr != null) {
                try {
                    Market market = Market.valueOf(marketStr.toUpperCase());
                    MarketExposure updated = exposureService.refreshExposureForMarket(market);
                    long count = processedEventCount.incrementAndGet();
                    log.info("[Kafka Exposure Projector] Updated exposure for market {}: openCount={}, estExposure={}, settledPayout={} (Total events: {})",
                            market, updated.getOpenClaimCount(), updated.getTotalEstimatedExposure(), updated.getTotalSettledPayout(), count);
                } catch (IllegalArgumentException e) {
                    log.warn("[Kafka Exposure Projector] Unknown market code received: {}", marketStr);
                }
            }
        }
    }

    public long getProcessedEventCount() {
        return processedEventCount.get();
    }

    public void resetEventCount() {
        processedEventCount.set(0);
    }
}
