package com.system.project1;

import com.system.project1.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.system.project1")
public class Project1Application {

	@Autowired
	private DiscountService discountService;

	public static void main(String[] args) {
		SpringApplication.run(Project1Application.class, args);
	}

	@Bean
	public CommandLineRunner initializeDefaults() {
		return args -> {
			// Initialize default discounts
			discountService.initializeDefaultDiscounts();
			System.out.println("Default discounts initialized successfully.");
		};
	}
}

// to run the programme = .\mvnw.cmd spring-boot:run