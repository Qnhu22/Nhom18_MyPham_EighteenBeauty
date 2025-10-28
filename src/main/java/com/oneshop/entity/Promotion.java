package com.oneshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long promotionId;

@Column(nullable = false, length = 255)
private String name; // Tên khuyến mãi (VD: "Sale 11.11", "Deal Cuối Tuần")

@Column(length = 2000)
private String description; // Mô tả nội dung khuyến mãi

@Column(precision = 5, scale = 2)
private BigDecimal discountPercent; // % giảm (VD: 10.00 = 10%)

@Column(precision = 12, scale = 2)
private BigDecimal discountAmount; // Giảm cố định (VNĐ)

@Column(nullable = false)
private LocalDateTime startDate;

@Column(nullable = false)
private LocalDateTime endDate;

@Column(nullable = false)
private Boolean status = true; // true = đang hoạt động

@Column(length = 50)
private String type; // "percent" | "amount" | "freeship" | ...

// 🔗 Một khuyến mãi có thể áp dụng cho nhiều sản phẩm
@OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL)
private List<Product> products;
}