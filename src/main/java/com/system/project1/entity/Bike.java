package com.system.project1.entity;

import jakarta.persistence.Entity;

@Entity
public class Bike extends Vehicle {
    private String bikeType;
    private int engineCapacity;

    public Bike(String vehicleID, String brand, String model, double rentPrice, String bikeType, int engineCapacity) {
        super(vehicleID, brand, model, rentPrice);
        this.bikeType = bikeType;
        this.engineCapacity = engineCapacity;
    }

    public Bike() {
    }

    public void setBikeType(String bikeType) {
        this.bikeType = bikeType;
    }

    public void setEngineCapacity(int engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    public String getBikeType() {
        return bikeType;
    }

    public int getEngineCapacity() {
        return engineCapacity;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Bike Type: " + bikeType);
        System.out.println("Engine capacity: " + engineCapacity);
    }
}
