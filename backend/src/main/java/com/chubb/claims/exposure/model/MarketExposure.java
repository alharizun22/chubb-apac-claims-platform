package com.chubb.claims.exposure.model;

import com.chubb.claims.claims.model.Market;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "market_exposure")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketExposure {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "market", nullable = false, length = 10)
    private Market market;

    @Column(name = "open_claim_count", nullable = false)
    @Builder.Default
    private long openClaimCount = 0L;

    @Column(name = "total_estimated_exposure", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalEstimatedExposure = BigDecimal.ZERO;

    @Column(name = "total_settled_payout", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalSettledPayout = BigDecimal.ZERO;

    @Column(name = "last_updated_at", nullable = false)
    @Builder.Default
    private Instant lastUpdatedAt = Instant.now();
}
