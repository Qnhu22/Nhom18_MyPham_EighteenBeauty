package com.oneshop.service;

import com.oneshop.dto.ChartData;
import com.oneshop.entity.Order;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    List<Order> getOrdersByShipperUserId(Long shipperUserId);
    List<Order> getOrdersByShipperUserIdAndStatus(Long shipperUserId, String status);
    Order getOrderByIdForShipper(Long orderId, Long shipperUserId);
    Order getOrderById(Long orderId);
    
    void updateOrderStatusHistoryByShipper(Long orderId, Long shipperUserId, String newStatus, String note);
    // thống kê
    long countDeliveredByShipper(Long shipperUserId);
    long countFailedByShipper(Long shipperUserId);
	BigDecimal calculateTotalRevenueByShipper(Long shipperId);
	long countOrdersByShipper(Long shipperUserId);
	long countTotalOrdersByShipper(Long shipperId);
	Order save(Order order);
	List<ChartData> getMonthlyDeliveredStats(Long shipperId);

    
}
