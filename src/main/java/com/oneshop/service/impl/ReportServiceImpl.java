package com.oneshop.service.impl;

import com.oneshop.entity.Order;
import com.oneshop.repository.OrderRepository;
import com.oneshop.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;

    @Override
    public byte[] generateRevenueReport(Long shipperId) {
    	List<Order> orders = orderRepository.findByShipper_ShipperId(shipperId);


        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Doanh thu giao hàng");

            // Header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã đơn");
            header.createCell(1).setCellValue("Khách hàng");
            header.createCell(2).setCellValue("Địa chỉ");
            header.createCell(3).setCellValue("Trạng thái");
            header.createCell(4).setCellValue("Tổng tiền");

            int rowNum = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getOrderId());
                row.createCell(1).setCellValue(order.getAddress() != null ? order.getAddress().getReceiverName() : "N/A");
                row.createCell(2).setCellValue(order.getAddress() != null ? order.getAddress().getAddressLine() : "N/A");
                row.createCell(3).setCellValue(order.getStatus().toString());
                row.createCell(4).setCellValue(order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tạo file Excel báo cáo", e);
        }
    }
}
