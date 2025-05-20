package com.system.project1.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;

@Entity
public class EventBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bookingId;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore // To prevent circular reference in JSON
    private Customer customer;

    @Temporal(TemporalType.TIMESTAMP)
    private Date bookingDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date eventDate;

    private String status; // "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"
    private double totalPrice;
    private String specialRequirements;

    // For transporting customer information
    @Transient
    private String customerName;

    @Transient
    private String contactNumber;

    // Constructors
    public EventBooking() {
        this.bookingDate = new Date();
        this.status = "PENDING";
    }

    public EventBooking(Event event, Customer customer, Date eventDate) {
        this.event = event;
        this.customer = customer;
        this.bookingDate = new Date();
        this.eventDate = eventDate;
        this.status = "PENDING";
        this.totalPrice = event.getPrice();
        // Generate a booking ID
        this.bookingId = "EV-" + System.currentTimeMillis();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getSpecialRequirements() {
        return specialRequirements;
    }

    public void setSpecialRequirements(String specialRequirements) {
        this.specialRequirements = specialRequirements;
    }

    public String getCustomerName() {
        return customer != null ? customer.getName() : customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactNumber() {
        return customer != null ? customer.getContactNumber() : contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
