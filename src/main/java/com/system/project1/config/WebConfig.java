package com.system.project1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Register resource handler for images
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        // Register resource handler for static resources
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        // Redirect root path to the login.html
        registry.addViewController("/").setViewName("forward:/login.html");

        // Add view controllers for each HTML page to handle direct URL access
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/register").setViewName("forward:/register.html");
        registry.addViewController("/profile").setViewName("forward:/profile.html");
        registry.addViewController("/inventory").setViewName("forward:/inventory.html");
        registry.addViewController("/rentals").setViewName("forward:/rentals.html");
    }
}