package com.oneshop.service;

import com.oneshop.dto.ChartData;
import com.oneshop.dto.PerformanceStats;
import com.oneshop.entity.Order;
import com.oneshop.entity.OrderItem;
import com.oneshop.entity.User;
import com.oneshop.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

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
    List<OrderItem> getOrderItems(Long orderId);

    // Phân công giao hàng cho shipper
    void assignShipper(Long orderId, Long shipperUserId);

    // Cập nhật trạng thái đơn hàng
    void updateOrderStatus(Long orderId, OrderStatus newStatus);

    // Xóa đơn hàng
    void deleteOrder(Long orderId);

    Page<Order> filterOrders(OrderStatus status, String keyword, LocalDate date, LocalDate startDate, LocalDate endDate, int page);

    Order findById(Long orderId);


    List<Order> getOrdersByUser(User user);
    List<Order> getOrdersByUserAndStatus(User user, OrderStatus status);
    Order saveOrder(Order order);
    boolean changeStatus(Long orderId, User owner, OrderStatus toStatus, String note);
}
