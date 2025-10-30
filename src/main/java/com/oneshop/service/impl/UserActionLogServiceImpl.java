package com.oneshop.service.impl;

import com.oneshop.dto.UserLogCountProjection;
import com.oneshop.entity.User;
import com.oneshop.entity.UserActionLog;
import com.oneshop.repository.UserActionLogRepository;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.UserActionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserActionLogServiceImpl implements UserActionLogService {

    private final UserActionLogRepository logRepository;
    private final UserRepository userRepository;

    @Override
    public void save(UserActionLog log) {
        logRepository.save(log);
    }

    public void logAction(User admin, User targetUser, String actionType, String description) {
        UserActionLog log = UserActionLog.builder()
                .admin(admin)
                .targetUser(targetUser)
                .actionType(actionType)
                .actionTime(LocalDateTime.now())
                .description(description)
                .build();
        logRepository.save(log);
    }

    public List<UserActionLog> getLogsByUser(Long userId) {
        return logRepository.findByTargetUserUserId(userId);
    }
    
    @Override
    public long countAll() {
        return userRepository.count();
    }

    @Override
    public long countByActive(boolean active) {
        return userRepository.countByActive(active);
    }

    @Override
    public long countByRole(String roleName) {
        return userRepository.countByRoles_RoleName(roleName);
    }
    
    @Override
    public List<Integer> getMonthlyUserRegistrations() {
        List<Object[]> result = userRepository.countUsersByMonthThisYear();

        // Khởi tạo mảng 12 tháng ban đầu là 0
        Map<Integer, Integer> monthMap = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthMap.put(i, 0);
        }

        // Gán lại số lượng từ kết quả DB
        for (Object[] row : result) {
            Integer month = (Integer) row[0];
            Long count = (Long) row[1];
            monthMap.put(month, count.intValue());
        }

        // Trả về danh sách dạng List<Integer> để binding vào biểu đồ
        return monthMap.values().stream().collect(Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> getTopActiveUsers() {
        List<UserLogCountProjection> projections = logRepository.findTop5UsersByLogCount();
        List<Map<String, Object>> result = new ArrayList<>();

        for (UserLogCountProjection proj : projections) {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", proj.getUserId());
            item.put("username", proj.getUsername());
            item.put("email", proj.getEmail());
            item.put("logCount", proj.getLogCount());
            result.add(item);
        }

        return result;
    }
}
