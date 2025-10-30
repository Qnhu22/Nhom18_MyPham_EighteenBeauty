package com.oneshop.repository;

import com.oneshop.entity.Order;
import com.oneshop.entity.User;
import com.oneshop.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

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

 // Count orders by paymentStatus
 	@Query("select count(o) from Order o where o.paymentStatus = :status")
 	long countByPaymentStatus(@Param("status") String status);


 	// Count orders by order status enum
 	long countByStatus(OrderStatus status);


 	// Sum totalAmount for paid + delivered between dates
 	@Query(value = "SELECT SUM(COALESCE(totalAmount,0)) FROM orders " +
 	"WHERE paymentStatus = 'PAID' AND status = 'DELIVERED' AND orderDate >= :from AND orderDate <= :to", nativeQuery = true)

    BigDecimal sumRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);


 	// Revenue grouped by day (SQL Server) -> returns list of [dateString, sum]
 	@Query(value = "SELECT FORMAT(orderDate, 'yyyy-MM-đ') as day, SUM(ISNULL(totalAmount,0)) as total " +
 	"FROM orders " +
 	"WHERE paymentStatus='PAID' AND status='DELIVERED' AND orderDate >= :from AND orderDate <= :to " +
 	"GROUP BY FORMAT(orderDate, 'yyyy-MM-đ') ORDER BY day", nativeQuery = true)
 	List<Object[]> revenueByDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);


 	// Revenue grouped by month (yyyy-MM)
 	@Query(value = "SELECT FROMAT(orderDate, 'yyyy-MM') as month, SUM(ISNULL(totalAmount, 0)) as total " +
 	"FROM orders " +
 	"WHERE paymentStatus='PAID' AND status='DELIVERED' AND orderDate >= :from AND orderDate <= :to " +
 	"GROUP BY FORMAT(orderDate, 'yyyy-MM') ORDER BY month", nativeQuery = true)
 	List<Object[]> revenueByMonth(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);


 	// Revenue grouped by week (SQL Server's DATEPART(iso_week,...))
 	@Query(value = "SELECT CONCAT(DATEPART(iso_week, orderDate), '-', DATEPART(year, orderDate)) as wk, SUM(ISNULL(totalAmount,0)) as total " +
 	"FROM orders " +
 	"WHERE paymentStatus='PAID' AND status='DELIVERED' AND orderDate >= :from AND orderDate <= :to " +
 	"GROUP BY DATEPART(iso_week, orderDate), DATEPART(year, orderDate) ORDER BY DATEPART(year, orderDate), DATEPART(iso_week, orderDate)", nativeQuery = true)
 	List<Object[]> revenueByWeek(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    List<Order> findByUserOrderByOrderDateDesc(User user);
    List<Order> findByUserAndStatusOrderByOrderDateDesc(User user, OrderStatus status);
}
