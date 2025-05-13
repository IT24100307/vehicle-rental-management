package com.system.project1.entity;

import java.util.Date;

public class PurchasedVehicle {
    private String purchaseId;
    private Vehicle vehicle;
    private String customerName;
    private String contactNumber;
    private Date purchaseDate;
    private boolean isRented;
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

    // Getters and Setters
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