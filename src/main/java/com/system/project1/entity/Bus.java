package com.system.project1.entity;

import jakarta.persistence.Entity;

@Entity
public class Bus extends Vehicle {
    int passengerCapacity;
    String hasWheelchairAccess;

    public Bus(String vehicleID, String brand, String model, double rentPrice, int passengerCapacity, String hasWheelchairAccess) {
        super(vehicleID, brand, model, rentPrice);
        this.passengerCapacity = passengerCapacity;
        this.hasWheelchairAccess = hasWheelchairAccess;
    }

    public Bus() {
    }

    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    public String getHasWheelchairAccess() {
        return hasWheelchairAccess;
    }

    public void setPassengerCapacity(int passengerCapacity) {
        this.passengerCapacity = passengerCapacity;
    }

    public void setHasWheelchairAccess(String hasWheelchairAccess) {
        this.hasWheelchairAccess = hasWheelchairAccess;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Passenger Capacity: "+passengerCapacity);
        System.out.println("Wheel chair Access: "+hasWheelchairAccess);
    }
}

