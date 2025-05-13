package com.system.project1.entity;

import jakarta.persistence.Entity;

@Entity
public class Van extends Vehicle {
    int NumberOfSeat;
    String hasSlidingDoors;

    public Van(String vehicleID, String brand, String model, double rentPrice, int numberOfSeat, String hasSlidingDoors) {
        super(vehicleID, brand, model, rentPrice);
        NumberOfSeat = numberOfSeat;
        this.hasSlidingDoors = hasSlidingDoors;
    }

    public Van() {
    }

    public int getNumberOfSeat() {
        return NumberOfSeat;
    }

    public String getHasSlidingDoors() {
        return hasSlidingDoors;
    }

    public void setNumberOfSeat(int numberOfSeat) {
        NumberOfSeat = numberOfSeat;
    }

    public void setHasSlidingDoors(String hasSlidingDoors) {
        this.hasSlidingDoors = hasSlidingDoors;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Cargo Capacity: "+NumberOfSeat);
        System.out.println("Door type: "+hasSlidingDoors);
    }
}
