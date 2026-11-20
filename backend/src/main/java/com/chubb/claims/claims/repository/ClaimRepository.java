package com.chubb.claims.claims.repository;

import com.chubb.claims.claims.model.Claim;
import com.chubb.claims.claims.model.ClaimStatus;
import com.chubb.claims.claims.model.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID>, JpaSpecificationExecutor<Claim> {

    Optional<Claim> findByReferenceNumber(String referenceNumber);

    List<Claim> findByClaimantIdOrderByCreatedAtDesc(String claimantId);

    List<Claim> findByAssignedOfficerIdOrderByCreatedAtDesc(String assignedOfficerId);

    List<Claim> findByStatusOrderByCreatedAtDesc(ClaimStatus status);

    List<Claim> findByMarketOrderByCreatedAtDesc(Market market);

    @Query("SELECT c FROM Claim c WHERE c.status = 'SUBMITTED' AND c.assignedOfficerId IS NULL ORDER BY c.createdAt ASC")
    List<Claim> findUnassignedClaims();

    long countByMarketAndStatusIn(Market market, Collection<ClaimStatus> statuses);

    @Query("SELECT COALESCE(SUM(c.estimatedAmount), 0) FROM Claim c WHERE c.market = :market AND c.status IN :statuses")
    BigDecimal sumEstimatedAmountByMarketAndStatusIn(@Param("market") Market market,
                                                     @Param("statuses") Collection<ClaimStatus> statuses);

    @Query("SELECT COALESCE(SUM(c.settledAmount), 0) FROM Claim c WHERE c.market = :market AND c.status = :status")
    BigDecimal sumSettledAmountByMarketAndStatus(@Param("market") Market market,
                                                @Param("status") ClaimStatus status);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.market = :market AND c.referenceNumber LIKE :prefix%")
    long countByMarketAndReferencePrefix(@Param("market") Market market,
                                         @Param("prefix") String prefix);
}
