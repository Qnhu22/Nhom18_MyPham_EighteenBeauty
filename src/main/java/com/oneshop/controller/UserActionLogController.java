package com.oneshop.controller;

import com.oneshop.entity.UserActionLog;
import com.oneshop.service.UserActionLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
public class UserActionLogController {
	
	private final UserActionLogService userActionLogService = null;

    /**
     * Hiển thị danh sách log thao tác của admin lên user cụ thể
     * @param userId ID người dùng cần xem lịch sử chỉnh sửa
     */
    @GetMapping("/user/{userId}")
    public String viewLogsForUser(@PathVariable Long userId, Model model) {
        List<UserActionLog> logs = userActionLogService.getLogsByUser(userId);
        model.addAttribute("logs", logs);
        model.addAttribute("userId", userId);
        return "admin/user-log-list"; // Tạo file này để hiển thị
    }
    
    @GetMapping("/top-users")
    public String viewTopUsersByLog(Model model) {
        List<Map<String, Object>> topUsers = userActionLogService.getTopActiveUsers();
        model.addAttribute("topUsers", topUsers);
        return "admin/user-log-stats"; // Tạo file HTML này
    }
}
