package com.oneshop.service.impl;

import com.oneshop.dto.UserLogCountProjection;
import com.oneshop.entity.OrderAddress;
import com.oneshop.entity.User;
import com.oneshop.repository.UserActionLogRepository;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService; // service gửi mail
    private final UserActionLogRepository logRepository;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           MailService mailService,
                           UserActionLogRepository logRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.logRepository = logRepository;
    }

    // ================== CRUD ==================
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    // Đăng ký user bình thường (active=false, cần OTP)
    @Override
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(false); // mặc định chưa kích hoạt
        return userRepository.save(user);
    }

    // Admin thêm user trực tiếp (active=true, không cần OTP)
    public User createUserByAdmin(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true); // active ngay, không cần OTP
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long userId, User user) {
        return userRepository.findById(userId)
                .map(existing -> {
                    existing.setFullName(user.getFullName());
                    existing.setEmail(user.getEmail());
                    existing.setPhone(user.getPhone());
                    if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                        existing.setPassword(passwordEncoder.encode(user.getPassword()));
                    }
                    existing.setActive(user.isActive());
                    return userRepository.save(existing);
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // ================== NGHIỆP VỤ ==================

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Sinh OTP 6 số ngẫu nhiên
    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    // Gửi OTP (cho đăng ký hoặc quên mật khẩu)
    public void sendOtp(User user) {
        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        mailService.sendOtp(user.getEmail(), otp);
        System.out.println("📧 [INFO] Đã gửi OTP " + otp + " cho email " + user.getEmail());
    }

    @Override
    public User activateUser(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setActive(true);
                    user.setOtpCode(null); // clear OTP sau khi active
                    user.setOtpExpiry(null);
                    return userRepository.save(user);
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public boolean verifyOtp(String email, String otpCode) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        if (user.getOtpCode() == null || user.getOtpExpiry() == null) return false;

        boolean valid = user.getOtpCode().equals(otpCode)
                && user.getOtpExpiry().isAfter(LocalDateTime.now());

        if (valid) {
            // nếu OTP hợp lệ thì active luôn
            user.setActive(true);
            user.setOtpCode(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
        }

        return valid;
    }

    @Override
    public void updatePassword(Long userId, String newPassword) {
        userRepository.findById(userId)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    return userRepository.save(user);
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Quên mật khẩu: gửi lại OTP
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            sendOtp(user);
        });
    }

    // Reset mật khẩu bằng OTP
    public boolean resetPassword(String email, String otpCode, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        if (user.getOtpCode() == null || user.getOtpExpiry() == null) return false;

        boolean valid = user.getOtpCode().equals(otpCode)
                && user.getOtpExpiry().isAfter(LocalDateTime.now());

        if (valid) {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setOtpCode(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    @Override
    public void setDefaultAddress(User user, OrderAddress address) {
        user.setDefaultAddress(address);
        userRepository.save(user);
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

        Map<Integer, Integer> monthMap = new HashMap<>();
        for (int i = 1; i <= 12; i++) monthMap.put(i, 0);

        for (Object[] row : result) {
            Integer month = (Integer) row[0];
            Long count = (Long) row[1];
            monthMap.put(month, count.intValue());
        }

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
    
    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }
    
    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> search(String keyword, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(keyword, keyword, pageable);
    }
}
