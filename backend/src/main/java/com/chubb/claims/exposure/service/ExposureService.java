package com.chubb.claims.exposure.service;

import com.chubb.claims.claims.model.ClaimStatus;
import com.chubb.claims.claims.model.Market;
import com.chubb.claims.claims.repository.ClaimRepository;
import com.chubb.claims.exposure.dto.MarketExposureResponse;
import com.chubb.claims.exposure.model.MarketExposure;
import com.chubb.claims.exposure.repository.MarketExposureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class ExposureService {

    private static final Logger log = LoggerFactory.getLogger(ExposureService.class);

    private static final Set<ClaimStatus> OPEN_STATUSES = EnumSet.of(
            ClaimStatus.SUBMITTED,
            ClaimStatus.UNDER_REVIEW,
            ClaimStatus.PENDING_INFO
    );

    private final MarketExposureRepository exposureRepository;
    private final ClaimRepository claimRepository;

    public ExposureService(MarketExposureRepository exposureRepository,
                           ClaimRepository claimRepository) {
        this.exposureRepository = exposureRepository;
        this.claimRepository = claimRepository;
    }

    @Transactional(readOnly = true)
    public List<MarketExposureResponse> getAllExposures() {
        return Arrays.stream(Market.values())
                .map(market -> exposureRepository.findByMarket(market)
                        .orElseGet(() -> MarketExposure.builder()
                                .market(market)
                                .openClaimCount(0L)
                                .totalEstimatedExposure(BigDecimal.ZERO)
                                .totalSettledPayout(BigDecimal.ZERO)
                                .lastUpdatedAt(Instant.now())
                                .build()))
                .map(MarketExposureResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public MarketExposureResponse getMarketExposure(Market market) {
        MarketExposure exposure = exposureRepository.findByMarket(market)
                .orElseGet(() -> MarketExposure.builder()
                        .market(market)
                        .openClaimCount(0L)
                        .totalEstimatedExposure(BigDecimal.ZERO)
                        .totalSettledPayout(BigDecimal.ZERO)
                        .lastUpdatedAt(Instant.now())
                        .build());
        return MarketExposureResponse.fromEntity(exposure);
    }

    @Transactional
    public MarketExposure refreshExposureForMarket(Market market) {
        long openCount = claimRepository.countByMarketAndStatusIn(market, OPEN_STATUSES);
        BigDecimal totalEstimated = claimRepository.sumEstimatedAmountByMarketAndStatusIn(market, OPEN_STATUSES);
        BigDecimal totalSettled = claimRepository.sumSettledAmountByMarketAndStatus(market, ClaimStatus.SETTLED);

        MarketExposure exposure = exposureRepository.findByMarket(market)
                .orElseGet(() -> MarketExposure.builder().market(market).build());

        exposure.setOpenClaimCount(openCount);
        exposure.setTotalEstimatedExposure(totalEstimated != null ? totalEstimated : BigDecimal.ZERO);
        exposure.setTotalSettledPayout(totalSettled != null ? totalSettled : BigDecimal.ZERO);
        exposure.setLastUpdatedAt(Instant.now());

        MarketExposure saved = exposureRepository.save(exposure);
        log.debug("Refreshed exposure for market {}: openCount={}, estExposure={}, settledPayout={}",
                market, openCount, totalEstimated, totalSettled);
        return saved;
    }

    @Transactional
    public void recalculateAll() {
        for (Market market : Market.values()) {
            refreshExposureForMarket(market);
        }
    }
}
