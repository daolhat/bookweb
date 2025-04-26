package com.programing.bookweb.enums;

public enum PaymentMethod {

    COD("Thanh toán khi nhận hàng"),
    ONLINE("Thanh toán trực tuyến");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
