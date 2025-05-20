package com.system.project1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Vehicle {
    @Id
    protected String vehicleID;
    private String brand;
    private String model;
    private double rentPrice;
    private String imagePath;
    private transient String originalVehicleID; // Transient means it won't be stored in the database
    private boolean isRented; // Track if vehicle is currently rented

    public Vehicle(String vehicleID, String brand, String model, double rentPrice) {
        this.vehicleID = vehicleID;
        this.brand = brand;
        this.model = model;
        this.rentPrice = rentPrice;
        this.imagePath = null; // Initialize image path as null
        this.isRented = false; // Not rented by default
    }

    public Vehicle() {
    }

    public String getVehicleID() {
        return vehicleID;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public double getRentPrice() {
        return rentPrice;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getOriginalVehicleID() {
        return originalVehicleID;
    }

    public boolean isRented() {
        return isRented;
    }

    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setRentPrice(double rentPrice) {
        this.rentPrice = rentPrice;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setOriginalVehicleID(String originalVehicleID) {
        this.originalVehicleID = originalVehicleID;
    }

    public void setRented(boolean rented) {
        isRented = rented;
    }

    public double calculateRent(int days) {
        double price = rentPrice * days;
        System.out.println("Rent price with days: " + price + " Rs");
        return price;
    }

    public double calculateRentWithDiscount(int days, double discountPercentage) {
        double originalPrice = calculateRent(days);
        double discountAmount = originalPrice * (discountPercentage / 100.0);
        double discountedPrice = originalPrice - discountAmount;
        System.out.println("Original price: " + originalPrice + " Rs");
        System.out.println("Discount: " + discountPercentage + "% (" + discountAmount + " Rs)");
        System.out.println("Discounted price: " + discountedPrice + " Rs");
        return discountedPrice;
    }

    public void isAvailable(String ID) {
        boolean status = vehicleID.equals(ID);
        if (status) {
            System.out.println("Vehicle is Available");
        } else {
            System.out.println("Vehicle is not available");
        }
    }

    public void displayDetails() {
        System.out.println("Vehicle ID: " + vehicleID);
        System.out.println("Brand Name: " + brand);
        System.out.println("Model: " + model);
        System.out.println("Rent price: " + rentPrice + " Rs");
        if (imagePath != null)
            System.out.println("Image: " + imagePath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Vehicle vehicle = (Vehicle) o;

        return vehicleID != null ? vehicleID.equals(vehicle.vehicleID) : vehicle.vehicleID == null;
    }

    @Override
    public int hashCode() {
        return vehicleID != null ? vehicleID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "vehicleID='" + vehicleID + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", rentPrice=" + rentPrice +
                '}';
    }
}