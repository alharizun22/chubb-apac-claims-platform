/**
 * Common Infrastructure & Shared Kernel Package.
 * Owns cross-cutting components:
 * - Domain event definitions published over Kafka
 * - Global exception handler and domain exceptions
 * - Pragmatic header-based RBAC filter (X-User-Id, X-User-Role)
 * - OpenAPI / Swagger UI configuration
 */
package com.chubb.claims.common;
