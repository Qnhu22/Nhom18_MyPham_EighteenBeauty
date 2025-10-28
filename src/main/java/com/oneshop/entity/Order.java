package com.oneshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "shipper_id")
    private Shipper shipper;

    private LocalDateTime orderDate;
    private String status; // CONFIRMED, SHIPPING, DELIVERED, CANCELLED, RETURNED
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private OrderAddress address;

    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;

    private String note;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
}
