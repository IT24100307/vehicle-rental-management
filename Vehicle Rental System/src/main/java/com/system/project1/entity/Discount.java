package com.system.project1.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double discountPercentage;
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private LocalDate startDate;
    private LocalDate endDate;

    // For loyalty discounts
    private Integer minimumRides;

    // For global discounts across all vehicles
    private boolean applyToAllVehicles;

    // Staff relationship - which staff member created/manages this discount
    private Long staffId;

    // Constructor
    public Discount() {
        this.isActive = true;
    }

    public Discount(String name, String description, double discountPercentage, DiscountType discountType) {
        this.name = name;
        this.description = description;
        this.discountPercentage = discountPercentage;
        this.discountType = discountType;
        this.isActive = true;
    }

    // Enum for discount types
    public enum DiscountType {
        SEASONAL,
        LOYALTY,
        GLOBAL
    }

    // Getters and Setters
    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getMinimumRides() {
        return minimumRides;
    }

    public void setMinimumRides(Integer minimumRides) {
        this.minimumRides = minimumRides;
    }

    public boolean isApplyToAllVehicles() {
        return applyToAllVehicles;
    }

    public void setApplyToAllVehicles(boolean applyToAllVehicles) {
        this.applyToAllVehicles = applyToAllVehicles;
    }
}