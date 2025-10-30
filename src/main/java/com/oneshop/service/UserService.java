package com.oneshop.service;

import com.oneshop.entity.OrderAddress;
import com.oneshop.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    // ================== CRUD ==================
    List<User> getAllUsers();
    Optional<User> getUserById(Long userId);
    User createUser(User user); // đăng ký bình thường (active=false, cần OTP)
    User createUserByAdmin(User user); // admin thêm trực tiếp (active=true, không cần OTP)
    User updateUser(Long userId, User user);
    void deleteUser(Long userId);

    // ================== NGHIỆP VỤ ==================
    Optional<User> findByEmail(String email);
    User activateUser(Long userId);  // kích hoạt tài khoản bằng OTP
    boolean verifyOtp(String email, String otpCode); // xác thực OTP
    void updatePassword(Long userId, String newPassword); // đổi mật khẩu
    void setDefaultAddress(User user, OrderAddress address);
    
    long countAll();
    long countByActive(boolean active);
    long countByRole(String roleName);
    List<Integer> getMonthlyUserRegistrations();
    List<Map<String, Object>> getTopActiveUsers();
	User getByUsername(String username);
}
