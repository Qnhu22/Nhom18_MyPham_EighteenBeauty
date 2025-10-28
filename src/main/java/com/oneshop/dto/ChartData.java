package com.oneshop.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartData {
	private String month;         // Tháng
    private long deliveredCount;  // Số đơn giao thành công
    private BigDecimal totalRevenue;  // Tổng doanh thu tháng đó
}
