package com.oneshop.repository;

import com.oneshop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	// Lấy danh sách đơn được gán cho shipper (theo userId của bảng users)
	List<Order> findByShipper_User_UserId(Long userId);

	// Lấy theo shipper + trạng thái
	List<Order> findByShipper_User_UserIdAndStatus(Long userId, String status);

	// Kiểm tra quyền truy cập (tìm order theo id và shipper)
	Optional<Order> findByOrderIdAndShipper_User_UserId(Long orderId, Long userId);

	@Query("SELECT COUNT(o) FROM Order o WHERE o.shipper.shipperId = :shipperId")
	long countByShipper(@Param("shipperId") Long shipperId);

	@Query("SELECT COUNT(o) FROM Order o WHERE o.shipper.shipperId = :shipperId AND o.status = :status")
	long countByShipperAndStatus(@Param("shipperId") Long shipperId, @Param("status") String status);

	@Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.shipper.shipperId = :shipperId AND o.status = 'DELIVERED'")
	BigDecimal sumTotalDeliveredAmountByShipper(@Param("shipperId") Long shipperId);

	@Query("SELECT FUNCTION('MONTH', o.orderDate) AS month, COUNT(o) " + "FROM Order o "
			+ "WHERE o.shipper.shipperId = :shipperId AND o.status = 'DELIVERED' "
			+ "GROUP BY FUNCTION('MONTH', o.orderDate) " + "ORDER BY FUNCTION('MONTH', o.orderDate)")
	List<Object[]> countMonthlyDeliveredByShipper(@Param("shipperId") Long shipperId);

	@Query("""
		    SELECT 
		        MONTH(o.orderDate) AS month,
		        COUNT(o.orderId) AS deliveredCount,
		        SUM(o.finalAmount) AS totalRevenue
		    FROM Order o
		    WHERE o.shipper.shipperId = :shipperId
		      AND o.status = 'DELIVERED'
		      AND YEAR(o.orderDate) = YEAR(CURRENT_DATE)
		    GROUP BY MONTH(o.orderDate)
		    ORDER BY MONTH(o.orderDate)
		""")
		List<Object[]> getMonthlyDeliveredStats(@Param("shipperId") Long shipperId);



		@Query("""
			    SELECT
			        SUM(CASE WHEN o.status = 'DELIVERED' THEN 1 ELSE 0 END),
			        SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END),
			        SUM(CASE WHEN o.status = 'RETURNED' THEN 1 ELSE 0 END)
			    FROM Order o
			    WHERE o.shipper.shipperId = :shipperId
			""")
			List<Object[]> getPerformanceStats(@Param("shipperId") Long shipperId);



}
