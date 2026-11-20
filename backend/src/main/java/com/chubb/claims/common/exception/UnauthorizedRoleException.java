package com.chubb.claims.common.exception;

/**
 * Thrown when an actor does not have the required role to execute a command.
 * Results in HTTP 403 Forbidden.
 */
public class UnauthorizedRoleException extends RuntimeException {

    public UnauthorizedRoleException(String message) {
        super(message);
    }
}
