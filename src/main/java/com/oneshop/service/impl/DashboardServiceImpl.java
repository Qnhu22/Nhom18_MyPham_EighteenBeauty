package com.oneshop.service.impl;

import com.oneshop.entity.ProductVariant;
import com.oneshop.enums.OrderStatus;
import com.oneshop.repository.OrderRepository;
import com.oneshop.repository.ProductVariantRepository;
import com.oneshop.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService{

	@Autowired
	private final OrderRepository orderRepo;
	@Autowired
	private final ProductVariantRepository variantRepo;
	
	public DashboardServiceImpl(OrderRepository orderRepo, ProductVariantRepository variantRepo) {
		this.orderRepo = orderRepo;
		this.variantRepo = variantRepo;
	}
	
	@Override
	public long totalOrders() {
		return orderRepo.count();
	}

	@Override
	public long ordersUnpaid() {
		return orderRepo.countByPaymentStatus("UNPAID");
	}

	@Override
	public long ordersPaid() {
		return orderRepo.countByPaymentStatus("PAID");
	}

	@Override
	public long ordersCancelled() {
		return orderRepo.countByStatus(OrderStatus.CANCELLED);
	}

	@Override
	public BigDecimal totalRevenue(LocalDateTime from, LocalDateTime to) {
		BigDecimal v = orderRepo.sumRevenueBetween(from, to);
		return v == null ? BigDecimal.ZERO : v;
	}

	@Override
	public Map<String, BigDecimal> revenueSeries(LocalDateTime from, LocalDateTime to, String granularity) {
		// granularity: day | week | month
		List<Object[]> rows;
		if ("month".equalsIgnoreCase(granularity)) {
		rows = orderRepo.revenueByMonth(from, to);
		} else if ("week".equalsIgnoreCase(granularity)) {
		rows = orderRepo.revenueByWeek(from, to);
		} else {
		rows = orderRepo.revenueByDay(from, to);
		}
		Map<String, BigDecimal> map = new LinkedHashMap<>();
		for (Object[] r : rows) {
		String key = (r[0] == null) ? "-" : r[0].toString();
		BigDecimal total = r[1] == null ? BigDecimal.ZERO : new BigDecimal(r[1].toString());
		map.put(key, total);
		}
		return map;
	}

	@Override
	public List<ProductVariant> topSellingVariants(int limit) {
		return variantRepo.findTopBySoldCount(PageRequest.of(0, limit));
	}

	@Override
	public long countByStatus(OrderStatus status) {
		return orderRepo.countByStatus(status);
	}
	
}
