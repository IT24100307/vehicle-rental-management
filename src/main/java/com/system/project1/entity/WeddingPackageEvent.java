package com.system.project1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "wedding_package_events")
public class WeddingPackageEvent extends Event {
    private String decorationStyle;
    private boolean includesDriver;

    // Default constructor for JPA
    public WeddingPackageEvent() {
        super();
    }

    public WeddingPackageEvent(String eventId, double basePrice, List<String> vehicleTypes, String decorationStyle,
            boolean includesDriver) {
        super(eventId, "Wedding Package", basePrice, vehicleTypes);
        this.decorationStyle = decorationStyle;
        this.includesDriver = includesDriver;
    }

    // For backward compatibility
    public WeddingPackageEvent(String eventId, double basePrice, String[] vehicleTypes, String decorationStyle,
            boolean includesDriver) {
        super(eventId, "Wedding Package", basePrice, Arrays.asList(vehicleTypes));
        this.decorationStyle = decorationStyle;
        this.includesDriver = includesDriver;
    }

    public String getDecorationStyle() {
        return decorationStyle;
    }

    public void setDecorationStyle(String decorationStyle) {
        this.decorationStyle = decorationStyle;
    }

    public boolean isIncludesDriver() {
        return includesDriver;
    }

    public void setIncludesDriver(boolean includesDriver) {
        this.includesDriver = includesDriver;
    }

    @Override
    public double calculateTotalPrice() {
        double price = getBasePrice();

        // Add cost for decoration
        if ("premium".equalsIgnoreCase(decorationStyle)) {
            price += 5000;
        } else if ("luxury".equalsIgnoreCase(decorationStyle)) {
            price += 10000;
        }

        // Add cost for driver
        if (includesDriver) {
            price += 2000;
        }

        return price;
    }

    @Override
    public void displayEventDetails() {
        super.displayEventDetails();
        System.out.println("Decoration Style: " + decorationStyle);
        System.out.println("Driver Included: " + (includesDriver ? "Yes" : "No"));
        System.out.println("Total Price: Rs. " + calculateTotalPrice());
        System.out.println("----------------------------------");
    }
}
