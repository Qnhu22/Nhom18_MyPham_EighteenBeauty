package com.oneshop.service;

import com.oneshop.dto.ChartData;
import com.oneshop.dto.PerformanceStats;
import com.oneshop.entity.Order;
import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    Order save(Order order);
    List<Order> getOrdersByShipperUserId(Long shipperId);
    List<Order> getOrdersByShipperUserIdAndStatus(Long shipperId, String status);
    Order getOrderByIdAndShipperId(Long orderId, Long shipperId);
    Order getOrderById(Long orderId);
    void updateOrderStatusByShipper(Long orderId, Long shipperId, String newStatus, String note);
    long countDeliveredByShipper(Long shipperId);
    long countFailedByShipper(Long shipperId);
    BigDecimal calculateTotalRevenueByShipper(Long shipperId);
    long countOrdersByShipper(Long shipperId);
    long countTotalOrdersByShipper(Long shipperId);
    List<ChartData> getMonthlyDeliveredStats(Long shipperId);
    PerformanceStats getPerformanceStats(Long shipperId);
}
