package com.system.project1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    private String cusID;

    private String cusName;
    private int contactNum;
    private int driverLicenseNumber;
    private int rentedVehicle;
    private int nodays;
    private boolean returnrented;

    public Customer() {
    }

    public Customer(String cusID, String cusName, int contactNum, int driverLicenseNumber, int rentedVehicle) {
        this.cusID = cusID;
        this.cusName = cusName;
        this.contactNum = contactNum;
        this.driverLicenseNumber = driverLicenseNumber;
        this.rentedVehicle = rentedVehicle;
    }

    // Getters and Setters
    public String getCusID() {

        return cusID;
    }

    public void setCusID(String cusID) {

        this.cusID = cusID;
    }

    public String getCusName() {

        return cusName;
    }

    public void setCusName(String cusName) {

        this.cusName = cusName;
    }

    public int getContactNum() {

        return contactNum;
    }

    public void setContactNum(int contactNum) {

        this.contactNum = contactNum;
    }

    public int getDriverLicenseNumber() {

        return driverLicenseNumber;
    }

    public void setDriverLicenseNumber(int driverLicenseNumber) {

        this.driverLicenseNumber = driverLicenseNumber;
    }

    public int getRentedVehicle() {

        return rentedVehicle;
    }

    public void setRentedVehicle(int rentedVehicle) {

        this.rentedVehicle = rentedVehicle;
    }

    public int getNodays() {

        return nodays;
    }

    public void setNodays(int nodays) {

        this.nodays = nodays;
    }

    public boolean isReturnrented() {

        return returnrented;
    }

    public void setReturnrented(boolean returnrented) {

        this.returnrented = returnrented;
    }
}
