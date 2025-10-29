package com.oneshop.enums;

public enum DiscountType {
    PERCENT("Giảm theo %"),
    AMOUNT("Giảm theo tiền"),
    FREESHIP("Miễn phí vận chuyển");

    private final String label;

    DiscountType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}