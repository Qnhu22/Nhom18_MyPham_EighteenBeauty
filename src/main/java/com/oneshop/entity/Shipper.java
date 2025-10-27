package com.oneshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shippers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipperId;

    @Column(length = 100)
    private String area;

    @Column(length = 20, nullable = false)
    private String status; 
    // AVAILABLE, DELIVERING, OFFLINE

    private Integer totalDelivered;
    private Integer totalFailed;
}

