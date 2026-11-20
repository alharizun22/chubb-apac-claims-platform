package com.chubb.claims.common.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Pragmatic header-based RBAC filter.
 * Extracts X-User-Id and X-User-Role (CLAIMANT | OFFICER | SYSTEM) from request headers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserContextFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String userId = request.getHeader(HEADER_USER_ID);
            String roleStr = request.getHeader(HEADER_USER_ROLE);

            UserRole role = UserRole.CLAIMANT;
            if (StringUtils.hasText(roleStr)) {
                try {
                    role = UserRole.valueOf(roleStr.trim().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    role = UserRole.CLAIMANT;
                }
            }

            if (!StringUtils.hasText(userId)) {
                userId = (role == UserRole.OFFICER) ? "officer-default" : "claimant-default";
            }

            UserContext context = UserContext.builder()
                    .userId(userId.trim())
                    .role(role)
                    .build();

            UserContextHolder.setContext(context);
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }
}
