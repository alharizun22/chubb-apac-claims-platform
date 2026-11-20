package com.chubb.claims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Chubb APAC Claims Processing Platform - Modular Monolith Entry Point.
 *
 * Domain Packages:
 * - claims: Core aggregate, lifecycle, submission, and state transitions
 * - assessment: Claims officer assessment, review, pickup, info request, settlement & rejection
 * - exposure: Market liability exposure read model and query projections
 * - notifications: Asynchronous claimant notifications & dispatch simulation
 * - common: Shared domain events, exceptions, header-based security, and Swagger configuration
 */
@SpringBootApplication
@EnableAsync
public class ChubbClaimsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChubbClaimsApplication.class, args);
    }
}
