package com.oneshop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
//import java.util.List;
import java.util.Map;

import com.oneshop.entity.ProductVariant;
import com.oneshop.enums.OrderStatus;

public interface DashboardService {
	
	long totalOrders();
	long ordersUnpaid();
	long ordersPaid();
	long ordersCancelled();
	BigDecimal totalRevenue(LocalDateTime from, LocalDateTime to);
	Map<String, BigDecimal> revenueSeries(LocalDateTime from, LocalDateTime to, String granularity);
	List<ProductVariant> topSellingVariants(int limit);
	long countByStatus(OrderStatus status);
}
