package com.chubb.claims.common.util;

import com.chubb.claims.claims.model.Market;
import com.chubb.claims.claims.repository.ClaimRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ReferenceNumberGenerator {

    private final ClaimRepository claimRepository;
    private final ConcurrentHashMap<String, AtomicLong> sequenceMap = new ConcurrentHashMap<>();

    public ReferenceNumberGenerator(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String generateReferenceNumber(Market market) {
        int year = LocalDate.now().getYear();
        String prefix = String.format("CHB-%s-%d-", market.name(), year);

        AtomicLong counter = sequenceMap.computeIfAbsent(prefix, k -> {
            long existingCount = claimRepository.countByMarketAndReferencePrefix(market, prefix);
            return new AtomicLong(existingCount);
        });

        long sequence = counter.incrementAndGet();
        return String.format("%s%04d", prefix, sequence);
    }
}
