package com.programing.bookweb.enums;

public enum OrderStatus {

    PENDING("Đang chờ xử lý"),
    PROCESSING("Đang xử lý"),
    DELIVERING("Đang giao hàng"),
    DELIVERED("Đã giao"),
    CANCELLED("Đã hủy");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static OrderStatus fromLabel(String label) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getLabel().equalsIgnoreCase(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trạng thái với nhãn: " + label);
    }
}
