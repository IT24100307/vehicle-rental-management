package com.system.project1.entity;

class RentalRecode{
    int recodeID;
    String vehicle;
    String customer;
    String rentDate;
    String returnDate;
    double totalCost;
    RentalRecode next;

    public RentalRecode(int recodeID, String vehicle, String customer, String rentDate, String returnDate, double totalCost) {
        this.recodeID = recodeID;
        this.vehicle = vehicle;
        this.customer = customer;
        this.rentDate = rentDate;
        this.returnDate = returnDate;
        this.totalCost = totalCost;
    }
    public void displayLink(){
        System.out.println("ID: "+recodeID);
        System.out.println("Vehicle: "+vehicle);
        System.out.println("Customer: "+customer);
        System.out.println("Rent date: "+rentDate);
        System.out.println("Return date: "+returnDate);
        System.out.println("Total cost: "+totalCost);
    }
}



