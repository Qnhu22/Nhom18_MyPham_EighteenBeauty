package com.oneshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shopId;

    @Column(nullable = false, columnDefinition = "nvarchar(255)")
    private String name; // Tên cửa hàng

    @Column(columnDefinition = "nvarchar(max)")
    private String description; // Mô tả ngắn

    @Column(length = 255)
    private String logoUrl; // Logo cửa hàng

    @Column(length = 100)
    private String email; // Email liên hệ

    @Column(length = 20)
    private String phone; // Số hotline

    @Column(length = 255)
    private String address; // Địa chỉ thực tế

    @Column(nullable = false)
    private boolean status = true; // 1 = hoạt động, 0 = tạm ngừng

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 🔗 Mỗi shop có 1 user làm Manager (1–1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", unique = true)
    private User manager;
}
