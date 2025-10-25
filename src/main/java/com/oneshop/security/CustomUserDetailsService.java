package com.oneshop.security;

import com.oneshop.entity.User;
import com.oneshop.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        System.out.println("🔎 [DEBUG] Đang tìm user với username/email: " + usernameOrEmail);

        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail)) // thử tìm theo email nếu không có username
                .orElseThrow(() -> {
                    System.err.println("❌ [ERROR] Không tìm thấy user với username/email: " + usernameOrEmail);
                    return new UsernameNotFoundException("Không tìm thấy user: " + usernameOrEmail);
                });

        System.out.println("✅ [INFO] Tìm thấy user: " + user.getUsername());
        System.out.println("👉 [INFO] Các role: " + user.getRoles().stream()
                .map(r -> r.getRoleName())
                .toList());

        return UserPrincipal.build(user);
    }
}
