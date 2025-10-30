package com.oneshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.oneshop.enums.PromotionType;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promotionId;

    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Boolean status;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PromotionType type; 
    // Enum PERCENT, AMOUNT, FREESHIP
}

