package com.oneshop.repository;

import com.oneshop.entity.Order;
import com.oneshop.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 🔹 Lấy danh sách đơn theo shipperId
    List<Order> findByShipper_ShipperId(Long shipperId);

    // 🔹 Lấy danh sách đơn theo shipperId + trạng thái
    List<Order> findByShipper_ShipperIdAndStatus(Long shipperId, OrderStatus status);

    // 🔹 Lấy 1 đơn cụ thể để kiểm tra quyền truy cập
    Optional<Order> findByOrderIdAndShipper_ShipperId(Long orderId, Long shipperId);

    // 🔹 Đếm số đơn của shipper
    long countByShipper_ShipperId(Long shipperId);

    // 🔹 Đếm số đơn theo trạng thái
    long countByShipper_ShipperIdAndStatus(Long shipperId, OrderStatus status);

    // 🔹 Tổng doanh thu đơn giao thành công
    @Query("""
        SELECT COALESCE(SUM(o.finalAmount), 0)
        FROM Order o
        WHERE o.shipper.id = :shipperId
          AND o.status = 'DELIVERED'
    """)
    BigDecimal sumTotalDeliveredAmountByShipper(@Param("shipperId") Long shipperId);

    // 🔹 Thống kê đơn giao hàng theo tháng
    @Query("""
        SELECT 
            MONTH(o.orderDate),
            COUNT(o.orderId),
            SUM(o.finalAmount)
        FROM Order o
        WHERE o.shipper.id = :shipperId
          AND o.status = 'DELIVERED'
          AND YEAR(o.orderDate) = YEAR(CURRENT_DATE)
        GROUP BY MONTH(o.orderDate)
        ORDER BY MONTH(o.orderDate)
    """)
    List<Object[]> getMonthlyDeliveredStats(@Param("shipperId") Long shipperId);

    // 🔹 Thống kê hiệu suất giao hàng
    @Query("""
        SELECT
            SUM(CASE WHEN o.status = 'DELIVERED' THEN 1 ELSE 0 END),
            SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END),
            SUM(CASE WHEN o.status = 'RETURNED' THEN 1 ELSE 0 END)
        FROM Order o
        WHERE o.shipper.id = :shipperId
    """)
    List<Object[]> getPerformanceStats(@Param("shipperId") Long shipperId);
}
