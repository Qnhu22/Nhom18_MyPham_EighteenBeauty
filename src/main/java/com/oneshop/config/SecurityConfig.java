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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Provider cho phép Spring Security dùng CustomUserDetailsService
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // AuthenticationManager (nếu cần dùng trong service)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // ✅ Cho phép truy cập công khai (không cần login)
                .requestMatchers(
                    "/", "/index", 
                    "/shop", "/shop/**",      // ⚙️ thêm dòng này
                    "/category/**", "/product/**",
                    "/register", "/verify-otp", 
                    "/forgot-password", "/reset-password",
                    "/login",
                    "/css/**", "/js/**", "/images/**", "/uploads/**",
                    "/decorators/**"
                ).permitAll()

                // 🔐 Các đường dẫn yêu cầu role
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/shipper/**").hasAnyRole("SHIPPER", "ADMIN")
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN", "MANAGER", "SHIPPER")

                // ✅ Cho phép các request còn lại (nếu có)
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .failureUrl("/login?error=true")
                .successHandler(successHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        // Gắn Custom Authentication Provider
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

}
