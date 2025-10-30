package com.oneshop.service;

import com.oneshop.entity.User;
import com.oneshop.entity.UserActionLog;

import java.util.List;
import java.util.Map;

public interface UserActionLogService {

    void save(UserActionLog log);

    void logAction(User admin, User targetUser, String actionType, String description);

    List<UserActionLog> getLogsByUser(Long userId);
    List<Map<String, Object>> getTopActiveUsers();
    long countAll();
    long countByActive(boolean active);
    long countByRole(String roleName);
    List<Integer> getMonthlyUserRegistrations(); // tháng 1–12          // top 5 người dùng
}
