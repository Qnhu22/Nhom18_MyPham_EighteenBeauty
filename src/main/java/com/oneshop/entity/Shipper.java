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

    @Column(nullable = false, length = 100)
    private String area;

    @Column(nullable = false, length = 20)
    private String status; // AVAILABLE, DELIVERING, OFFLINE

    private int totalDelivered;
    private int totalFailed;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

	
}
