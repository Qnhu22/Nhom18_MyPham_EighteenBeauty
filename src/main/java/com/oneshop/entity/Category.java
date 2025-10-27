package com.oneshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String name; // tên danh mục

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description; // mô tả ngắn

    @Column(length = 255)
    private String imageUrl; // ảnh đại diện danh mục

    @Column(nullable = false)
    private boolean status = true; // 1 = hiển thị, 0 = ẩn

    @CreationTimestamp
    private LocalDateTime createdAt; // ngày tạo danh mục

    @UpdateTimestamp
    private LocalDateTime updatedAt; // ngày cập nhật gần nhất

    // 🔗 Quan hệ 1 - N với Product
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Product> products = new HashSet<>();
}
