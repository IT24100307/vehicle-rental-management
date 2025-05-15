package com.system.project1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "long_trip_events")
public class LongTripEvent extends Event {
    private int totalKilometers;
    private int numberOfDays;

    // Default constructor for JPA
    public LongTripEvent() {
        super();
    }

    public LongTripEvent(String eventId, double basePrice, List<String> vehicleTypes, int totalKilometers,
            int numberOfDays) {
        super(eventId, "Long Trip", basePrice, vehicleTypes);
        this.totalKilometers = totalKilometers;
        this.numberOfDays = numberOfDays;
    }

    // For backward compatibility
    public LongTripEvent(String eventId, double basePrice, String[] vehicleTypes, int totalKilometers,
            int numberOfDays) {
        super(eventId, "Long Trip", basePrice, Arrays.asList(vehicleTypes));
        this.totalKilometers = totalKilometers;
        this.numberOfDays = numberOfDays;
    }

    public int getTotalKilometers() {
        return totalKilometers;
    }

    public void setTotalKilometers(int totalKilometers) {
        this.totalKilometers = totalKilometers;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    @Override
    public double calculateTotalPrice() {
        double price = getBasePrice();
        // Additional cost for distance
        if (totalKilometers > 100) {
            price += (totalKilometers - 100) * 15; // Rs. 15 per additional km beyond 100km
        }
        // Additional cost for days
        price += (numberOfDays - 1) * 1000; // Rs. 1000 per day after the first day

        return price;
    }

    @Override
    public void displayEventDetails() {
        super.displayEventDetails();
        System.out.println("Total Kilometers: " + totalKilometers);
        System.out.println("Number of Days: " + numberOfDays);
        System.out.println("Total Price: Rs. " + calculateTotalPrice());
        System.out.println("----------------------------------");
    }
}
