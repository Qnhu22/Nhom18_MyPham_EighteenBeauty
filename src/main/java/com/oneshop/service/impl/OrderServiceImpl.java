package com.oneshop.service.impl;

import com.oneshop.dto.ChartData;
import com.oneshop.dto.PerformanceStats;
import com.oneshop.entity.Order;
import com.oneshop.entity.OrderStatusHistory;
import com.oneshop.enums.OrderStatus;
import com.oneshop.repository.OrderRepository;
import com.oneshop.repository.OrderStatusHistoryRepository;
import com.oneshop.service.OrderService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderStatusHistoryRepository historyRepository;

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersByShipperUserId(Long shipperId) {
        return orderRepository.findByShipper_ShipperId(shipperId);
    }

    @Override
    public List<Order> getOrdersByShipperUserIdAndStatus(Long shipperId, String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status);
        return orderRepository.findByShipper_ShipperIdAndStatus(shipperId, orderStatus);
    }

    @Override
    public Order getOrderByIdAndShipperId(Long orderId, Long shipperId) {
        return orderRepository.findByOrderIdAndShipper_ShipperId(orderId, shipperId).orElse(null);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Override
    @Transactional
    public void updateOrderStatusByShipper(Long orderId, Long shipperId, String newStatus, String note) {
        Optional<Order> optionalOrder = orderRepository.findByOrderIdAndShipper_ShipperId(orderId, shipperId);
        if (!optionalOrder.isPresent()) return;

        Order order = optionalOrder.get();
        OrderStatus oldStatus = order.getStatus(); // lưu trạng thái cũ

        // 1️⃣ Cập nhật trạng thái enum
        OrderStatus newStatusEnum = OrderStatus.valueOf(newStatus);
        order.setStatus(newStatusEnum);

        // 2️⃣ Nếu chuyển sang DELIVERED mà paymentStatus = "UNPAID" thì chuyển thành "PAID"
        if (newStatusEnum == OrderStatus.DELIVERED && "UNPAID".equals(order.getPaymentStatus())) {
            order.setPaymentStatus("PAID");
        }

        orderRepository.save(order);

        // 3️⃣ Cập nhật lịch sử trong order_status_history (lưu String để tiện đọc)
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .changedBy(null) // shipper entity nếu có
                .oldStatus(oldStatus.name())  // lưu String
                .newStatus(newStatusEnum.name())
                .note(note)
                .changeAt(java.time.LocalDateTime.now())
                .build();
        historyRepository.save(history);
    }


    @Override
    public long countDeliveredByShipper(Long shipperId) {
        return orderRepository.countByShipper_ShipperIdAndStatus(shipperId, OrderStatus.DELIVERED);
    }

    @Override
    public long countFailedByShipper(Long shipperId) {
        return orderRepository.countByShipper_ShipperIdAndStatus(shipperId, OrderStatus.CANCELLED)
             + orderRepository.countByShipper_ShipperIdAndStatus(shipperId, OrderStatus.RETURNED);
    }

    @Override
    public BigDecimal calculateTotalRevenueByShipper(Long shipperId) {
        return orderRepository.sumTotalDeliveredAmountByShipper(shipperId);
    }

    @Override
    public long countOrdersByShipper(Long shipperId) {
        return orderRepository.countByShipper_ShipperId(shipperId);
    }

    @Override
    public long countTotalOrdersByShipper(Long shipperId) {
        return orderRepository.countByShipper_ShipperId(shipperId);
    }

    @Override
    public List<ChartData> getMonthlyDeliveredStats(Long shipperId) {
        List<Object[]> results = orderRepository.getMonthlyDeliveredStats(shipperId);
        List<ChartData> chartData = new ArrayList<>();
        for (Object[] r : results) {
            int month = (Integer) r[0];
            long count = ((Number) r[1]).longValue();
            BigDecimal revenue = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
            chartData.add(new ChartData("Tháng " + month, count, revenue));
        }
        return chartData;
    }

    @Override
    public PerformanceStats getPerformanceStats(Long shipperId) {
        List<Object[]> results = orderRepository.getPerformanceStats(shipperId);
        if (results.isEmpty()) return new PerformanceStats(0, 0, 0);
        Object[] r = results.get(0);
        return new PerformanceStats(
            ((Number) r[0]).longValue(),
            ((Number) r[1]).longValue(),
            ((Number) r[2]).longValue()
        );
    }
}
