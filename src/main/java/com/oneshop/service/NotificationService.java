package com.oneshop.service;

public interface NotificationService {
    void notifyUserOrderStatusChanged(Long userId, Long orderId, String newStatus, String note);
    void notifySellerOrderStatusChanged(Long sellerUserId, Long orderId, String newStatus, String note);
}
