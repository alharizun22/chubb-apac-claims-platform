package com.chubb.claims.assessment.service;

import com.chubb.claims.assessment.dto.ProvideInfoRequest;
import com.chubb.claims.assessment.dto.RejectClaimRequest;
import com.chubb.claims.assessment.dto.RequestInfoRequest;
import com.chubb.claims.assessment.dto.SettleClaimRequest;
import com.chubb.claims.claims.dto.ClaimDetailResponse;
import com.chubb.claims.claims.model.ActionType;
import com.chubb.claims.claims.model.Claim;
import com.chubb.claims.claims.model.ClaimStatus;
import com.chubb.claims.claims.repository.ClaimRepository;
import com.chubb.claims.claims.service.ClaimService;
import com.chubb.claims.claims.service.ClaimStateMachine;
import com.chubb.claims.common.auth.UserContext;
import com.chubb.claims.common.auth.UserRole;
import com.chubb.claims.common.event.*;
import com.chubb.claims.common.exception.ResourceNotFoundException;
import com.chubb.claims.common.exception.UnauthorizedRoleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AssessmentService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentService.class);

    private final ClaimRepository claimRepository;
    private final ClaimStateMachine stateMachine;
    private final ClaimService claimService;
    private final DomainEventPublisher eventPublisher;

    public AssessmentService(ClaimRepository claimRepository,
                             ClaimStateMachine stateMachine,
                             ClaimService claimService,
                             DomainEventPublisher eventPublisher) {
        this.claimRepository = claimRepository;
        this.stateMachine = stateMachine;
        this.claimService = claimService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ClaimDetailResponse pickupClaim(UUID claimId, UserContext actor) {
        enforceRole(actor, UserRole.OFFICER, "pickup claim");

        Claim claim = findClaim(claimId);
        stateMachine.validateTransition(claim.getStatus(), ClaimStatus.UNDER_REVIEW);

        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        claim.setAssignedOfficerId(actor.getUserId());
        Claim saved = claimRepository.save(claim);

        claimService.recordActivity(
                saved.getId(),
                actor.getUserId(),
                actor.getRole(),
                ActionType.PICKED_UP,
                "Claim picked up by officer " + actor.getUserId()
        );

        eventPublisher.publish(ClaimAssignedEvent.builder()
                .claimId(saved.getId())
                .referenceNumber(saved.getReferenceNumber())
                .market(saved.getMarket().name())
                .officerId(actor.getUserId())
                .build());

        log.info("Claim {} ({}) picked up by officer {}", claimId, saved.getReferenceNumber(), actor.getUserId());
        return claimService.getClaimDetail(claimId);
    }

    @Transactional
    public ClaimDetailResponse requestInfo(UUID claimId, RequestInfoRequest request, UserContext actor) {
        enforceRole(actor, UserRole.OFFICER, "request additional information");

        Claim claim = findClaim(claimId);
        stateMachine.validateTransition(claim.getStatus(), ClaimStatus.PENDING_INFO);

        claim.setStatus(ClaimStatus.PENDING_INFO);
        Claim saved = claimRepository.save(claim);

        claimService.recordActivity(
                saved.getId(),
                actor.getUserId(),
                actor.getRole(),
                ActionType.INFO_REQUESTED,
                request.getNote().trim()
        );

        eventPublisher.publish(ClaimInfoRequestedEvent.builder()
                .claimId(saved.getId())
                .referenceNumber(saved.getReferenceNumber())
                .market(saved.getMarket().name())
                .officerId(actor.getUserId())
                .note(request.getNote().trim())
                .build());

        log.info("Info requested for claim {} ({}) by officer {}", claimId, saved.getReferenceNumber(), actor.getUserId());
        return claimService.getClaimDetail(claimId);
    }

    @Transactional
    public ClaimDetailResponse provideInfo(UUID claimId, ProvideInfoRequest request, UserContext actor) {
        enforceRole(actor, UserRole.CLAIMANT, "provide additional information");

        Claim claim = findClaim(claimId);
        stateMachine.validateTransition(claim.getStatus(), ClaimStatus.UNDER_REVIEW);

        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        Claim saved = claimRepository.save(claim);

        claimService.recordActivity(
                saved.getId(),
                actor.getUserId(),
                actor.getRole(),
                ActionType.INFO_PROVIDED,
                request.getNote().trim()
        );

        eventPublisher.publish(ClaimInfoProvidedEvent.builder()
                .claimId(saved.getId())
                .referenceNumber(saved.getReferenceNumber())
                .market(saved.getMarket().name())
                .claimantId(actor.getUserId())
                .note(request.getNote().trim())
                .build());

        log.info("Info provided for claim {} ({}) by claimant {}", claimId, saved.getReferenceNumber(), actor.getUserId());
        return claimService.getClaimDetail(claimId);
    }

    @Transactional
    public ClaimDetailResponse settleClaim(UUID claimId, SettleClaimRequest request, UserContext actor) {
        enforceRole(actor, UserRole.OFFICER, "settle claim");

        Claim claim = findClaim(claimId);
        stateMachine.validateTransition(claim.getStatus(), ClaimStatus.SETTLED);

        claim.setStatus(ClaimStatus.SETTLED);
        claim.setSettledAmount(request.getSettledAmount());
        Claim saved = claimRepository.save(claim);

        String note = String.format("Claim settled for %s %s.%s",
                saved.getMarket().name(),
                request.getSettledAmount(),
                request.getNote() != null ? " Remarks: " + request.getNote().trim() : "");

        claimService.recordActivity(
                saved.getId(),
                actor.getUserId(),
                actor.getRole(),
                ActionType.SETTLED,
                note
        );

        eventPublisher.publish(ClaimDecidedEvent.builder()
                .claimId(saved.getId())
                .referenceNumber(saved.getReferenceNumber())
                .market(saved.getMarket().name())
                .decision("SETTLED")
                .settledAmount(request.getSettledAmount())
                .reason(request.getNote())
                .officerId(actor.getUserId())
                .build());

        log.info("Claim {} ({}) settled for {} by officer {}",
                claimId, saved.getReferenceNumber(), request.getSettledAmount(), actor.getUserId());
        return claimService.getClaimDetail(claimId);
    }

    @Transactional
    public ClaimDetailResponse rejectClaim(UUID claimId, RejectClaimRequest request, UserContext actor) {
        enforceRole(actor, UserRole.OFFICER, "reject claim");

        Claim claim = findClaim(claimId);
        stateMachine.validateTransition(claim.getStatus(), ClaimStatus.REJECTED);

        claim.setStatus(ClaimStatus.REJECTED);
        Claim saved = claimRepository.save(claim);

        String note = "Claim rejected. Reason: " + request.getReason().trim();

        claimService.recordActivity(
                saved.getId(),
                actor.getUserId(),
                actor.getRole(),
                ActionType.REJECTED,
                note
        );

        eventPublisher.publish(ClaimDecidedEvent.builder()
                .claimId(saved.getId())
                .referenceNumber(saved.getReferenceNumber())
                .market(saved.getMarket().name())
                .decision("REJECTED")
                .settledAmount(null)
                .reason(request.getReason().trim())
                .officerId(actor.getUserId())
                .build());

        log.info("Claim {} ({}) rejected by officer {}", claimId, saved.getReferenceNumber(), actor.getUserId());
        return claimService.getClaimDetail(claimId);
    }

    private Claim findClaim(UUID claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId));
    }

    private void enforceRole(UserContext actor, UserRole requiredRole, String action) {
        if (actor.getRole() != requiredRole && actor.getRole() != UserRole.SYSTEM) {
            throw new UnauthorizedRoleException(String.format(
                    "Access denied: Role '%s' is required to %s. Current actor has role '%s'.",
                    requiredRole, action, actor.getRole()));
        }
    }
}
