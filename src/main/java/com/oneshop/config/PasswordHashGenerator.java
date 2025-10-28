package com.oneshop.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 🧩 Mật khẩu cần hash
        String rawPassword = "shipper123";

        // 🔐 Hash mật khẩu
        String hashedPassword = encoder.encode(rawPassword);

        // 🖨️ In ra console
        System.out.println("Mật khẩu gốc: " + rawPassword);
        System.out.println("BCrypt hash: " + hashedPassword);
    }
}
