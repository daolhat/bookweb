package com.programing.bookweb.enums;

public enum ContactStatus {
    PENDING("Đang chờ xử lý"),
    PROCESSING("Đang xử lý"),
    PROCESSED("Đã xử lý");

    private final String label;

    ContactStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
