package com.system.project1.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;

@Entity
public class PurchasedVehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String purchaseId;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private int rentalDays;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore // To prevent circular reference in JSON
    private Customer customer;

    @Transient // This field is not persisted to the database
    private String customerName;

    @Transient // This field is not persisted to the database
    private String contactNumber;

    @Temporal(TemporalType.TIMESTAMP)
    private Date purchaseDate;

    private boolean isRented;

    @Transient // This field is not persisted to the database
    private PurchasedVehicle next; // Reference to the next node in the linked list

    public PurchasedVehicle() {
        this.purchaseDate = new Date();
        this.isRented = false;
        this.next = null;
    }

    public PurchasedVehicle(String purchaseId, Vehicle vehicle, String customerName, String contactNumber) {
        this.purchaseId = purchaseId;
        this.vehicle = vehicle;
        this.customerName = customerName;
        this.contactNumber = contactNumber;
        this.purchaseDate = new Date();
        this.isRented = false;
        this.next = null;
    }

    public PurchasedVehicle(Vehicle vehicle, int days, Customer customer) {
        this.purchaseId = generatePurchaseId();
        this.vehicle = vehicle;
        this.customer = customer;

        // Handle potential null values safely
        if (customer != null) {
            this.customerName = customer.getName();
            this.contactNumber = customer.getContactNumber();
        }

        this.rentalDays = days;
        this.purchaseDate = new Date();
        this.isRented = true;
        this.next = null;

        // Ensure we have some basic identification info if it's missing
        if (this.customerName == null && customer != null) {
            this.customerName = "Customer #" + customer.getCustomerId();
        }

        if (this.contactNumber == null) {
            this.contactNumber = "Not provided";
        }
    }

    private String generatePurchaseId() {
        return "P" + System.currentTimeMillis();
    } // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public void setRentalDays(int rentalDays) {
        this.rentalDays = rentalDays;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public boolean isRented() {
        return isRented;
    }

    public void setRented(boolean rented) {
        isRented = rented;
    }

    public PurchasedVehicle getNext() {
        return next;
    }

    public void setNext(PurchasedVehicle next) {
        this.next = next;
    }

    @Override
    public String toString() {
        return "PurchasedVehicle{" +
                "purchaseId='" + purchaseId + '\'' +
                ", vehicleId='" + (vehicle != null ? vehicle.getVehicleID() : "null") + '\'' +
                ", customerName='" + customerName + '\'' +
                ", isRented=" + isRented +
                '}';
    }
}