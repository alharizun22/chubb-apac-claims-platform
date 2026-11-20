package com.chubb.claims.common.exception;

/**
 * Thrown when an illegal state machine transition is attempted.
 * Results in HTTP 409 Conflict.
 */
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public InvalidStateTransitionException(String currentStatus, String targetStatus, String reason) {
        super(String.format("Invalid claim state transition from '%s' to '%s': %s", currentStatus, targetStatus, reason));
    }
}
