package com.chubb.claims.claims.repository;

import com.chubb.claims.claims.model.ClaimActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimActivityRepository extends JpaRepository<ClaimActivity, UUID> {

    List<ClaimActivity> findByClaimIdOrderByCreatedAtDesc(UUID claimId);

    List<ClaimActivity> findByClaimIdOrderByCreatedAtAsc(UUID claimId);
}
