package com.system.project1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.Column;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Event {
    @Id
    private String eventId;

    @Column(nullable = false)
    private String eventType; // Long Trip, Short Trip, Airport, Special

    @Column(nullable = false)
    private double basePrice;

    @Column
    private LocalDateTime scheduledDate;

    @Column
    private String status = "PENDING"; // Default status

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_vehicle_types", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "vehicle_type")
    private List<String> vehicleTypes = new ArrayList<>();

    // Default constructor required by JPA
    public Event() {
    }

    // Constructor
    public Event(String eventId, String eventType, double basePrice, List<String> vehicleTypes) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.basePrice = basePrice;
        if (vehicleTypes != null) {
            this.vehicleTypes.addAll(vehicleTypes);
        }
    }

    // Constructor with scheduledDate
    public Event(String eventId, String eventType, double basePrice, List<String> vehicleTypes,
            LocalDateTime scheduledDate) {
        this(eventId, eventType, basePrice, vehicleTypes);
        this.scheduledDate = scheduledDate;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public List<String> getVehicleTypes() {
        return vehicleTypes;
    }

    public void setVehicleTypes(List<String> vehicleTypes) {
        this.vehicleTypes.clear();
        if (vehicleTypes != null) {
            this.vehicleTypes.addAll(vehicleTypes);
        }
    }

    public void addVehicleType(String vehicleType) {
        if (vehicleType != null && !vehicleType.trim().isEmpty()) {
            this.vehicleTypes.add(vehicleType);
        }
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Method to calculate total price - will be overridden by subclasses
    public double calculateTotalPrice() {
        return basePrice;
    }

    public void displayEventDetails() {
        System.out.println("Event ID: " + eventId);
        System.out.println("Event Type: " + eventType);
        System.out.println("Base Price: Rs. " + basePrice);
        System.out.println("Scheduled Date: " + scheduledDate);
        System.out.println("Status: " + status);
        System.out.println("Available Vehicles: ");
        for (String vehicle : vehicleTypes) {
            System.out.println("  - " + vehicle);
        }
        System.out.println("----------------------------------");
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", basePrice=" + basePrice +
                ", scheduledDate=" + scheduledDate +
                ", status='" + status + '\'' +
                ", vehicleTypes=" + vehicleTypes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Event event = (Event) o;
        return Objects.equals(eventId, event.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}