package com.system.project1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int customerId;

    private String name;
    private String contactNumber;
    private int driverLicenseNumber;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String address;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<PurchasedVehicle> rentedVehiclesList;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<EventBooking> eventBookings;

    // Constructors
    public Customer() {
        this.rentedVehiclesList = new ArrayList<>();
        this.eventBookings = new ArrayList<>();
    }

    public Customer(String name, String contactNumber, int driverLicenseNumber, String email, String password) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.driverLicenseNumber = driverLicenseNumber;
        this.email = email;
        this.password = password;
        this.rentedVehiclesList = new ArrayList<>();
        this.eventBookings = new ArrayList<>();
    }

    // Methods
    public void rentVehicle(Vehicle v, int days) {
        PurchasedVehicle purchasedVehicle = new PurchasedVehicle(v, days, this);
        rentedVehiclesList.add(purchasedVehicle);
    }

    public void bookEvent(Event event, java.util.Date eventDate) {
        EventBooking booking = new EventBooking(event, this, eventDate);
        eventBookings.add(booking);
    }

    // Getters and Setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public int getDriverLicenseNumber() {
        return driverLicenseNumber;
    }

    public void setDriverLicenseNumber(int driverLicenseNumber) {
        this.driverLicenseNumber = driverLicenseNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<PurchasedVehicle> getRentedVehiclesList() {
        return rentedVehiclesList;
    }

    public void setRentedVehiclesList(List<PurchasedVehicle> rentedVehiclesList) {
        this.rentedVehiclesList = rentedVehiclesList;
    }

    public List<EventBooking> getEventBookings() {
        return eventBookings;
    }

    public void setEventBookings(List<EventBooking> eventBookings) {
        this.eventBookings = eventBookings;
    }

    public void viewRentedHistory() {
        for (PurchasedVehicle pv : rentedVehiclesList) {
            System.out.println(pv);
        }
    }
}
