package com.chubb.claims.exposure.repository;

import com.chubb.claims.claims.model.Market;
import com.chubb.claims.exposure.model.MarketExposure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketExposureRepository extends JpaRepository<MarketExposure, Market> {

    Optional<MarketExposure> findByMarket(Market market);
}
