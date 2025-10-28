package com.oneshop.service.impl;

import com.oneshop.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void notifyUserOrderStatusChanged(Long userId, Long orderId, String newStatus, String note) {
        // TODO: gắn mail hoặc hệ thống notification thật
        log.info("Notify user {}: order {} status -> {} : {}", userId, orderId, newStatus, note);
    }

    @Override
    public void notifySellerOrderStatusChanged(Long sellerUserId, Long orderId, String newStatus, String note) {
        log.info("Notify seller {}: order {} status -> {} : {}", sellerUserId, orderId, newStatus, note);
    }
}
