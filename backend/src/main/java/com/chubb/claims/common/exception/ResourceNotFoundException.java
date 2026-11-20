package com.chubb.claims.common.exception;

/**
 * Thrown when an entity (e.g. Claim) is not found.
 * Results in HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("%s with identifier '%s' was not found", resourceName, id));
    }
}
