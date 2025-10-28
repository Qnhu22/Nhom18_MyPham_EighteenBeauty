package com.oneshop.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_action_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adminId")
    private User admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User targetUser;

    @Column(columnDefinition = "nvarchar(20)")
    private String actionType;

    private LocalDateTime actionTime;

    @Column(columnDefinition = "nvarchar(200)")
    private String description;
}

