package com.system.project1.entity;

import jakarta.persistence.Entity;

@Entity
public class Lorry extends Vehicle {
    int maxLoadCapacity;
    int numAxles;

    public Lorry(String vehicleID, String brand, String model, double rentPrice, int maxLoadCapacity, int numAxles) {
        super(vehicleID, brand, model, rentPrice);
        this.maxLoadCapacity = maxLoadCapacity;
        this.numAxles = numAxles;
    }

    public Lorry() {
    }

    public int getMaxLoadCapacity() {
        return maxLoadCapacity;
    }

    public void setMaxLoadCapacity(int maxLoadCapacity) {
        this.maxLoadCapacity = maxLoadCapacity;
    }

    public void setNumAxles(int numAxles) {
        this.numAxles = numAxles;
    }

    public int getNumAxles() {
        return numAxles;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Max load capacity: "+maxLoadCapacity);
        System.out.println("Number of axles: "+numAxles);
    }
}

