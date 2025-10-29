package com.oneshop.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.oneshop.enums.OrderStatus;
import com.oneshop.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager/dashboard")
@RequiredArgsConstructor
public class ManagerDashboardController {
	private final DashboardService dashboardService;

	@GetMapping
	public String dashboard(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "day") String granularity, Model model) {

		// default: current month
		if (from == null) {
			from = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
		}
		if (to == null) {
			to = LocalDateTime.now().with(LocalTime.MAX);
		}

		long totalOrders = dashboardService.totalOrders();
		long unpaid = dashboardService.ordersUnpaid();
		long paid = dashboardService.ordersPaid();
		long cancelled = dashboardService.ordersCancelled();
		BigDecimal revenue = dashboardService.totalRevenue(from, to);
		Map<String, BigDecimal> series = dashboardService.revenueSeries(from, to, granularity);
		var top = dashboardService.topSellingVariants(8);
		
		List<String> variantNames = top.stream()
		        .map(v -> v.getName())
		        .toList();

		List<Integer> soldCounts = top.stream()
		        .map(v -> v.getSoldCount())
		        .toList();


		model.addAttribute("variantNames", variantNames);
		model.addAttribute("soldCounts", soldCounts);
		model.addAttribute("totalOrders", totalOrders);
		model.addAttribute("unpaid", unpaid);
		model.addAttribute("paid", paid);
		model.addAttribute("cancelled", cancelled);
		model.addAttribute("revenue", revenue);
		model.addAttribute("series", series);
		//model.addAttribute("topVariants", top);
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("granularity", granularity);

		model.addAttribute("pendingCount", dashboardService.countByStatus(OrderStatus.NEW));
		model.addAttribute("confirmedCount", dashboardService.countByStatus(OrderStatus.CONFIRMED));
		model.addAttribute("shippingCount", dashboardService.countByStatus(OrderStatus.SHIPPING));
		model.addAttribute("deliveredCount", dashboardService.countByStatus(OrderStatus.DELIVERED));
		model.addAttribute("canceledCount", dashboardService.countByStatus(OrderStatus.CANCELLED));
		model.addAttribute("returnedCount", dashboardService.countByStatus(OrderStatus.RETURNED));

		return "manager/dashboard"; // templates/manager/dashboard.html
	}
}
