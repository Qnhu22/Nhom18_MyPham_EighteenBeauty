package com.oneshop.enums;

public enum OrderStatus {
	NEW,        // Chờ xác nhận
    CONFIRMED,      // Đã xác nhận
    SHIPPING,        // Đang giao
    DELIVERED,      // Đã giao
    CANCELLED,       // Đã hủy
    RETURNED        // Đã trả hàng
}