package com.oneshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.oneshop.enums.DiscountType;
import com.oneshop.enums.VoucherStatus;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voucherId;

    @Column(length = 50, nullable = false, unique = true)
    private String code; // Mã voucher

    @Column(columnDefinition = "nvarchar(255)")
    private String name; // Tên voucher

    @Column(columnDefinition = "nvarchar(max)")
    private String description; // Mô tả / điều kiện chi tiết

    // 🔹 Kiểu giảm giá: PERCENT / AMOUNT / FREESHIP
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private DiscountType discountType;

    // 🔹 Nếu là PERCENT → % giảm
    private Integer discountPercent;

    // 🔹 Nếu là AMOUNT → số tiền giảm cố định
    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount;

    // 🔹 Giới hạn giảm tối đa (áp dụng cho %)
    @Column(precision = 12, scale = 2)
    private BigDecimal maxDiscountValue;

    // 🔹 Giá trị đơn hàng tối thiểu để áp dụng
    @Column(precision = 12, scale = 2)
    private BigDecimal minOrderValue;

    // 🔹 Giới hạn số lần sử dụng
    private Integer usageLimit;

    // 🔹 Đã sử dụng bao nhiêu lần
    private Integer usedCount;

    // 🔹 Thời gian hiệu lực
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // 🔹 Trạng thái (ACTIVE, EXPIRED, USED_UP, DISABLED,…)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VoucherStatus status;

    // 🔹 Ngày tạo
    private LocalDateTime createdAt;
}
