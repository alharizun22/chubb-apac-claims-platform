package com.chubb.claims.claims.service;

import com.chubb.claims.claims.dto.ClaimActivityDto;
import com.chubb.claims.claims.dto.ClaimDetailResponse;
import com.chubb.claims.claims.dto.ClaimResponse;
import com.chubb.claims.claims.dto.SubmitClaimRequest;
import com.chubb.claims.claims.model.*;
import com.chubb.claims.claims.repository.ClaimActivityRepository;
import com.chubb.claims.claims.repository.ClaimRepository;
import com.chubb.claims.common.auth.UserContext;
import com.chubb.claims.common.auth.UserRole;
import com.chubb.claims.common.event.ClaimSubmittedEvent;
import com.chubb.claims.common.event.DomainEventPublisher;
import com.chubb.claims.common.exception.ResourceNotFoundException;
import com.chubb.claims.common.util.ReferenceNumberGenerator;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);

    private final ClaimRepository claimRepository;
    private final ClaimActivityRepository activityRepository;
    private final ReferenceNumberGenerator referenceNumberGenerator;
    private final DomainEventPublisher eventPublisher;

    public ClaimService(ClaimRepository claimRepository,
                        ClaimActivityRepository activityRepository,
                        ReferenceNumberGenerator referenceNumberGenerator,
                        DomainEventPublisher eventPublisher) {
        this.claimRepository = claimRepository;
        this.activityRepository = activityRepository;
        this.referenceNumberGenerator = referenceNumberGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ClaimDetailResponse submitClaim(SubmitClaimRequest request, UserContext actor) {
        log.info("Submitting new claim for market={} by actor={}", request.getMarket(), actor.getUserId());

        String claimantId = StringUtils.hasText(request.getClaimantId())
                ? request.getClaimantId().trim()
                : actor.getUserId();

        String refNumber = referenceNumberGenerator.generateReferenceNumber(request.getMarket());

        Claim claim = Claim.builder()
                .referenceNumber(refNumber)
                .market(request.getMarket())
                .claimType(request.getClaimType())
                .policyNumber(request.getPolicyNumber().trim())
                .claimantName(request.getClaimantName().trim())
                .claimantId(claimantId)
                .incidentDate(request.getIncidentDate())
                .description(request.getDescription().trim())
                .estimatedAmount(request.getEstimatedAmount())
                .status(ClaimStatus.SUBMITTED)
                .build();

        Claim savedClaim = claimRepository.save(claim);

        // Record initial activity
        ClaimActivity activity = ClaimActivity.builder()
                .claimId(savedClaim.getId())
                .actorId(actor.getUserId())
                .actorRole(actor.getRole())
                .actionType(ActionType.SUBMITTED)
                .note("Claim submitted: " + request.getDescription().trim())
                .build();
        ClaimActivity savedActivity = activityRepository.save(activity);

        // Publish domain event
        eventPublisher.publish(ClaimSubmittedEvent.builder()
                .claimId(savedClaim.getId())
                .referenceNumber(savedClaim.getReferenceNumber())
                .market(savedClaim.getMarket().name())
                .claimType(savedClaim.getClaimType().name())
                .claimantId(savedClaim.getClaimantId())
                .claimantName(savedClaim.getClaimantName())
                .estimatedAmount(savedClaim.getEstimatedAmount())
                .build());

        return ClaimDetailResponse.fromEntity(savedClaim, List.of(ClaimActivityDto.fromEntity(savedActivity)));
    }

    @Transactional(readOnly = true)
    public ClaimDetailResponse getClaimDetail(UUID id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", id));

        List<ClaimActivityDto> activities = activityRepository.findByClaimIdOrderByCreatedAtAsc(id).stream()
                .map(ClaimActivityDto::fromEntity)
                .toList();

        return ClaimDetailResponse.fromEntity(claim, activities);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> listClaims(Market market,
                                          ClaimStatus status,
                                          String claimantId,
                                          Boolean unassignedOnly,
                                          String assignedOfficerId) {
        Specification<Claim> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (market != null) {
                predicates.add(cb.equal(root.get("market"), market));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(claimantId)) {
                predicates.add(cb.equal(root.get("claimantId"), claimantId.trim()));
            }
            if (Boolean.TRUE.equals(unassignedOnly)) {
                predicates.add(cb.and(
                        cb.equal(root.get("status"), ClaimStatus.SUBMITTED),
                        cb.isNull(root.get("assignedOfficerId"))
                ));
            }
            if (StringUtils.hasText(assignedOfficerId)) {
                predicates.add(cb.equal(root.get("assignedOfficerId"), assignedOfficerId.trim()));
            }

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return claimRepository.findAll(spec).stream()
                .map(ClaimResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ClaimActivity recordActivity(UUID claimId,
                                        String actorId,
                                        UserRole actorRole,
                                        ActionType actionType,
                                        String note) {
        ClaimActivity activity = ClaimActivity.builder()
                .claimId(claimId)
                .actorId(actorId)
                .actorRole(actorRole)
                .actionType(actionType)
                .note(note)
                .build();
        return activityRepository.save(activity);
    }
}
