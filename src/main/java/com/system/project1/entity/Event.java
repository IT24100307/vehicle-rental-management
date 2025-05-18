package com.system.project1.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private String eventType; // AIRPORT_DROP, WEDDING, PHOTOSHOOT, etc.
    private int durationHours;
    private String imagePath;
    private boolean isActive;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Transient
    private String startDateStr;

    @Transient
    private String endDateStr;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "event_vehicles", joinColumns = @JoinColumn(name = "event_id"), inverseJoinColumns = @JoinColumn(name = "vehicle_id"))
    private List<Vehicle> vehicles = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<EventBooking> bookings = new ArrayList<>();

    @Transient
    private List<String> vehicleIds;

    // Constructors
    public Event() {
        this.isActive = true;
        this.vehicleIds = new ArrayList<>();
    }

    public Event(String name, String description, double price, String eventType, int durationHours) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.eventType = eventType;
        this.durationHours = durationHours;
        this.isActive = true;
        this.vehicleIds = new ArrayList<>();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getStartDateStr() {
        return startDateStr;
    }

    public void setStartDateStr(String startDateStr) {
        this.startDateStr = startDateStr;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getEndDateStr() {
        return endDateStr;
    }

    public void setEndDateStr(String endDateStr) {
        this.endDateStr = endDateStr;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public void addVehicle(Vehicle vehicle) {
        this.vehicles.add(vehicle);
    }

    public void removeVehicle(Vehicle vehicle) {
        this.vehicles.remove(vehicle);
    }

    public List<EventBooking> getBookings() {
        return bookings;
    }

    public void setBookings(List<EventBooking> bookings) {
        this.bookings = bookings;
    }

    public void addBooking(EventBooking booking) {
        this.bookings.add(booking);
    }

    public List<String> getVehicleIds() {
        return vehicleIds;
    }

    public void setVehicleIds(List<String> vehicleIds) {
        this.vehicleIds = vehicleIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Event event = (Event) o;

        return id != null ? id.equals(event.id) : event.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", eventType='" + eventType + '\'' +
                ", startDate=" + startDate +
                ", startDateStr='" + startDateStr + '\'' +
                ", endDate=" + endDate +
                ", endDateStr='" + endDateStr + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", vehicleIds=" + (vehicleIds != null ? vehicleIds.size() : 0) +
                '}';
    }
}
