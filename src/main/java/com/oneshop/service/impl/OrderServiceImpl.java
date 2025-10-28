package com.oneshop.service.impl;

import com.oneshop.dto.ChartData;
import com.oneshop.entity.Order;
import com.oneshop.repository.OrderRepository;
import com.oneshop.service.OrderService;

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
   
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    // Lấy danh sách đơn của shipper theo userId
    public List<Order> getOrdersByShipperUserId(Long shipperUserId) {
        return orderRepository.findByShipper_User_UserId(shipperUserId);
    }

    // Lấy danh sách đơn của shipper theo userId + trạng thái
    public List<Order> getOrdersByShipperUserIdAndStatus(Long shipperUserId, String status) {
        return orderRepository.findByShipper_User_UserIdAndStatus(shipperUserId, status);
    }

    // Lấy 1 order theo id và shipper userId (kiểm tra quyền truy cập)
    public Order getOrderByIdForShipper(Long orderId, Long shipperUserId) {
        Optional<Order> optionalOrder = orderRepository.findByOrderIdAndShipper_User_UserId(orderId, shipperUserId);
        return optionalOrder.orElse(null);
    }

    // Lấy 1 order theo id
    public Order getOrderById(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        return optionalOrder.orElse(null);
    }

    // Cập nhật trạng thái order (ví dụ từ shipper)
    public void updateOrderStatusHistoryByShipper(Long orderId, Long shipperUserId, String newStatus, String note) {
        Optional<Order> optionalOrder = orderRepository.findByOrderIdAndShipper_User_UserId(orderId, shipperUserId);
        if(optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(newStatus);
           
            // nếu có field note hoặc history, có thể update ở đây
            orderRepository.save(order);
        }
    }

    // Thống kê số đơn DELIVERED của shipper
    
    public long countDeliveredByShipper(Long shipperUserId) {
        return orderRepository.countByShipperAndStatus(shipperUserId, "DELIVERED");
    }

    // Thống kê số đơn FAILED của shipper
    
    public long countFailedByShipper(Long shipperUserId) {
        return orderRepository.countByShipperAndStatus(shipperUserId, "FAILED");
    }

    // Tính tổng doanh thu của shipper
    public BigDecimal calculateTotalRevenueByShipper(Long shipperId) {
        return orderRepository.sumTotalDeliveredAmountByShipper(shipperId);
    }

    // Tổng số đơn theo shipper userId
    public long countOrdersByShipper(Long shipperUserId) {
        return orderRepository.countByShipper(shipperUserId);
    }

    public long countTotalOrdersByShipper(Long shipperId) {
        return orderRepository.countByShipper(shipperId);
    }
    @Override
    public List<ChartData> getMonthlyDeliveredStats(Long shipperId) {
        List<Object[]> results = orderRepository.countMonthlyDeliveredByShipper(shipperId);

        List<ChartData> chartData = new ArrayList<>();
        for (Object[] row : results) {
            String month = row[0].toString();
            long count = ((Number) row[1]).longValue();
            chartData.add(new ChartData(month, count));
        }

        return chartData;
    }

}
