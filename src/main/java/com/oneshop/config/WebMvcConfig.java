package com.oneshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map URL /uploads/** tới thư mục uploads trong project root
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
        // Nếu bạn lưu ngoài C:/ thì dùng:
        // .addResourceLocations("file:C:/uploads/");
    }
}
