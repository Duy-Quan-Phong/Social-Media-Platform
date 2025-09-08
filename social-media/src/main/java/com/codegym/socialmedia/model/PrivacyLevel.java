package com.codegym.socialmedia.model;

public enum PrivacyLevel {
    PUBLIC("Công khai"),
    FRIENDS("Bạn bè"),
    PRIVATE("Chỉ mình tôi");

    private final String displayName;

    PrivacyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
