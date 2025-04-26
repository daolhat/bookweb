package com.programing.bookweb.enums;

public enum PaymentStatus {

    PAID("Đã thanh toán"),
    UNPAID("Chưa thanh toán");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
