package com.system.project1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "airport_transfer_events")
public class AirportTransferEvent extends Event {
    private String airportName;
    private boolean isRoundTrip;

    // Default constructor for JPA
    public AirportTransferEvent() {
        super();
    }

    public AirportTransferEvent(String eventId, double basePrice, List<String> vehicleTypes, String airportName,
            boolean isRoundTrip) {
        super(eventId, "Airport Transfer", basePrice, vehicleTypes);
        this.airportName = airportName;
        this.isRoundTrip = isRoundTrip;
    }

    // For backward compatibility
    public AirportTransferEvent(String eventId, double basePrice, String[] vehicleTypes, String airportName,
            boolean isRoundTrip) {
        super(eventId, "Airport Transfer", basePrice, Arrays.asList(vehicleTypes));
        this.airportName = airportName;
        this.isRoundTrip = isRoundTrip;
    }

    public String getAirportName() {
        return airportName;
    }

    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    public boolean isRoundTrip() {
        return isRoundTrip;
    }

    public void setRoundTrip(boolean roundTrip) {
        isRoundTrip = roundTrip;
    }

    @Override
    public double calculateTotalPrice() {
        double price = getBasePrice();
        if (isRoundTrip) {
            price *= 1.8; // 80% additional charge for round trip
        }
        return price;
    }

    @Override
    public void displayEventDetails() {
        super.displayEventDetails();
        System.out.println("Airport Name: " + airportName);
        System.out.println("Round Trip: " + (isRoundTrip ? "Yes" : "No"));
        System.out.println("Total Price: Rs. " + calculateTotalPrice());
        System.out.println("----------------------------------");
    }
}
