package com.chubb.claims.common.auth;

public final class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void setContext(UserContext context) {
        CONTEXT.set(context);
    }

    public static UserContext getContext() {
        UserContext ctx = CONTEXT.get();
        if (ctx == null) {
            return UserContext.builder()
                    .userId("anonymous")
                    .role(UserRole.CLAIMANT)
                    .build();
        }
        return ctx;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
