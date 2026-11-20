package com.chubb.claims.exposure.dto;

import com.chubb.claims.claims.model.Market;
import com.chubb.claims.exposure.model.MarketExposure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketExposureResponse {
    private Market market;
    private String marketDisplayName;
    private long openClaimCount;
    private BigDecimal totalEstimatedExposure;
    private BigDecimal totalSettledPayout;
    private Instant lastUpdatedAt;

    public static MarketExposureResponse fromEntity(MarketExposure entity) {
        return MarketExposureResponse.builder()
                .market(entity.getMarket())
                .marketDisplayName(entity.getMarket().getDisplayName())
                .openClaimCount(entity.getOpenClaimCount())
                .totalEstimatedExposure(entity.getTotalEstimatedExposure())
                .totalSettledPayout(entity.getTotalSettledPayout())
                .lastUpdatedAt(entity.getLastUpdatedAt())
                .build();
    }
}
