package com.system.project1.entity;

import jakarta.persistence.Entity;

@Entity
public class Car extends Vehicle {
    private int numSeats;
    private String hasAC;
    private String transmissionType;

    public Car(String vehicleID, String brand, String model, double rentPrice, int numSeats, String hasAC, String transmissionType, String imagePath) {
        super(vehicleID, brand, model, rentPrice);
        this.numSeats = numSeats;
        this.hasAC = hasAC;
        this.transmissionType = transmissionType;
    }

    public Car() {
    }

    public int getNumSeats() {
        return numSeats;
    }

    public String getHasAC() {
        return hasAC;
    }

    public String getTransmissionType() {
        return transmissionType;
    }

    public void setNumSeats(int numSeats) {
        this.numSeats = numSeats;
    }

    public void setHasAC(String hasAC) {
        this.hasAC = hasAC;
    }

    public void setTransmissionType(String transmissionType) {
        this.transmissionType = transmissionType;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Number of Seats: " + numSeats);
        System.out.println("AC: " + hasAC);
        System.out.println("Transmission Type: " + transmissionType);
    }
}