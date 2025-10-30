package com.oneshop.config;

import com.oneshop.security.CustomAuthenticationSuccessHandler;
import com.oneshop.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler,
                          CustomUserDetailsService userDetailsService) {
        this.successHandler = successHandler;
        this.userDetailsService = userDetailsService;
    }

    // ✅ Mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ Provider cho phép Spring Security dùng CustomUserDetailsService
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // ✅ AuthenticationManager (nếu cần dùng trong service)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ✅ Cấu hình bảo mật
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // tắt CSRF cho đơn giản khi dev
            .authorizeHttpRequests(auth -> auth
                // 🔓 Cho phép truy cập công khai
                .requestMatchers(
                    "/", "/index",
                    "/about",                    // 🆕 Thêm Giới thiệu
                    "/shop", "/shop/**",
                    "/category/**", "/product/**",
                    "/register", "/verify-otp",
                    "/forgot-password", "/reset-password",
                    "/login",
                    "/css/**", "/js/**", "/images/**", "/uploads/**"
                ).permitAll()

                .requestMatchers("/api/wishlist/**").authenticated()
                // 🔐 Giới hạn quyền cho các khu vực riêng
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasRole("MANAGER")
                .requestMatchers("/shipper/**").hasRole("SHIPPER")
                .requestMatchers("/user/**").hasRole("USER")

                // Các request khác cho phép truy cập
                .anyRequest().permitAll()
            )
            // 🔑 Form login
            .formLogin(form -> form
                .loginPage("/login")
                .failureUrl("/login?error=true")
                .successHandler(successHandler)
                .permitAll()
            )
            // 🚪 Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        // Gắn Custom Provider
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}
