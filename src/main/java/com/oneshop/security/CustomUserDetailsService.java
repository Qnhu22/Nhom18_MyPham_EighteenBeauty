package com.oneshop.security;

import com.oneshop.entity.User;
import com.oneshop.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional // ❌ bỏ readOnly để có thể cập nhật DB
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        System.out.println("🔎 [DEBUG] Đang tìm user với username/email: " + usernameOrEmail);

        // ✅ Ưu tiên tìm theo username, nếu không có thì tìm theo email
        User user = userRepository.findByUsernameFetchRoles(usernameOrEmail)
                .or(() -> userRepository.findByEmailFetchRoles(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + usernameOrEmail));

        System.out.println("✅ [INFO] Tìm thấy user: " + user.getUsername());
        System.out.println("👉 [INFO] Roles: " + user.getRoles().stream()
                .map(r -> r.getRoleName())
                .toList());

        // 🕒 Cập nhật lần đăng nhập gần nhất
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user); // lưu lại vào DB

        // ✅ Trả về đối tượng UserDetails cho Spring Security
        return UserPrincipal.build(user);
    }
}
