package com.oneshop.service;

import com.oneshop.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    // ================== CRUD ==================
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    User createUser(User user); // đăng ký bình thường (active=false, cần OTP)
    User createUserByAdmin(User user); // admin thêm trực tiếp (active=true, không cần OTP)
    User updateUser(Long id, User user);
    void deleteUser(Long id);

    // ================== NGHIỆP VỤ ==================
    Optional<User> findByEmail(String email);
    User activateUser(Long userId);  // kích hoạt tài khoản bằng OTP
    boolean verifyOtp(String email, String otpCode); // xác thực OTP
    void updatePassword(Long userId, String newPassword); // đổi mật khẩu
	User getByUsername(String name);
}
