package com.chubb.claims.claims.model;

public enum Market {
    MY("Malaysia"),
    SG("Singapore"),
    TH("Thailand"),
    HK("Hong Kong"),
    AU("Australia"),
    JP("Japan");

    private final String displayName;

    Market(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
