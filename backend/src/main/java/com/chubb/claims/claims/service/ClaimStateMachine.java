package com.chubb.claims.claims.service;

import com.chubb.claims.claims.model.ClaimStatus;
import com.chubb.claims.common.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Strict Claim State Machine.
 * Validates and enforces legal state transitions:
 * - SUBMITTED -> UNDER_REVIEW (Officer pickup)
 * - UNDER_REVIEW -> PENDING_INFO (Officer requests info)
 * - PENDING_INFO -> UNDER_REVIEW (Claimant provides info)
 * - UNDER_REVIEW -> SETTLED (Officer settles)
 * - UNDER_REVIEW -> REJECTED (Officer rejects)
 *
 * Any other transition raises an InvalidStateTransitionException (HTTP 409 Conflict).
 */
@Component
public class ClaimStateMachine {

    private static final Map<ClaimStatus, Set<ClaimStatus>> VALID_TRANSITIONS = new EnumMap<>(ClaimStatus.class);

    static {
        // From SUBMITTED only pickup into UNDER_REVIEW is legal
        VALID_TRANSITIONS.put(ClaimStatus.SUBMITTED, EnumSet.of(ClaimStatus.UNDER_REVIEW));

        // From UNDER_REVIEW an officer can request info, settle, or reject
        VALID_TRANSITIONS.put(ClaimStatus.UNDER_REVIEW, EnumSet.of(
                ClaimStatus.PENDING_INFO,
                ClaimStatus.SETTLED,
                ClaimStatus.REJECTED
        ));

        // From PENDING_INFO only fulfillment back into UNDER_REVIEW is legal
        VALID_TRANSITIONS.put(ClaimStatus.PENDING_INFO, EnumSet.of(ClaimStatus.UNDER_REVIEW));

        // Terminal states
        VALID_TRANSITIONS.put(ClaimStatus.SETTLED, Collections.emptySet());
        VALID_TRANSITIONS.put(ClaimStatus.REJECTED, Collections.emptySet());
    }

    /**
     * Asserts that transitioning from currentStatus to targetStatus is permitted.
     *
     * @param currentStatus Current status of the claim
     * @param targetStatus Proposed target status
     * @throws InvalidStateTransitionException if transition is disallowed (HTTP 409)
     */
    public void validateTransition(ClaimStatus currentStatus, ClaimStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new InvalidStateTransitionException("Current and target claim statuses must not be null");
        }

        if (currentStatus == targetStatus) {
            throw new InvalidStateTransitionException(
                    currentStatus.name(),
                    targetStatus.name(),
                    "Claim is already in status " + currentStatus.name()
            );
        }

        Set<ClaimStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet());
        if (!allowed.contains(targetStatus)) {
            if (currentStatus.isTerminal()) {
                throw new InvalidStateTransitionException(
                        currentStatus.name(),
                        targetStatus.name(),
                        String.format("Claim is in terminal status '%s' and cannot transition to '%s'", currentStatus, targetStatus)
                );
            }
            throw new InvalidStateTransitionException(
                    currentStatus.name(),
                    targetStatus.name(),
                    String.format("Transition from '%s' to '%s' is not allowed. Valid next states are %s",
                            currentStatus, targetStatus, allowed)
            );
        }
    }
}
