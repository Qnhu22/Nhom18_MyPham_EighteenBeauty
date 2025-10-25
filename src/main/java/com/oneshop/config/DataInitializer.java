package com.oneshop.config;

import com.oneshop.entity.Role;
import com.oneshop.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            List<String> defaultRoles = Arrays.asList(
                    "ROLE_USER",
                    "ROLE_ADMIN",
                    "ROLE_MANAGER",
                    "ROLE_SHIPPER"
            );

            for (String roleName : defaultRoles) {
                roleRepository.findByRoleName(roleName)
                        .orElseGet(() -> {
                            System.out.println("👉 Creating role: " + roleName);
                            return roleRepository.save(Role.builder()
                                    .roleName(roleName)
                                    .build());
                        });
            }

            System.out.println("✅ Default roles initialized!");
        };
    }
}
