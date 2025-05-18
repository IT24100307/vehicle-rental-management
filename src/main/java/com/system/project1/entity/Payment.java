package com.system.project1.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentId;
    private String paymentMethod; // "CARD" or "CASH"
    private double amount;

    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;

    // Card specific fields
    private String cardHolderName;
    private String cardNumber; // Storing last 4 digits only for reference
    private String cardExpiry;
    private String cardType; // VISA, MASTER, etc.

    // Relationship with PurchasedVehicle
    @OneToOne
    @JoinColumn(name = "purchased_vehicle_id")
    private PurchasedVehicle purchasedVehicle;

    // Relationship with EventBooking
    @OneToOne
    @JoinColumn(name = "event_booking_id")
    private EventBooking eventBooking;

    // Status of payment
    private String status; // "PENDING", "COMPLETED", "FAILED"

    public Payment() {
        this.paymentDate = new Date();
        this.status = "PENDING";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        // Store only last 4 digits for security
        if (cardNumber != null && cardNumber.length() > 4) {
            this.cardNumber = "xxxx-xxxx-xxxx-" + cardNumber.substring(cardNumber.length() - 4);
        } else {
            this.cardNumber = cardNumber;
        }
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public PurchasedVehicle getPurchasedVehicle() {
        return purchasedVehicle;
    }

    public void setPurchasedVehicle(PurchasedVehicle purchasedVehicle) {
        this.purchasedVehicle = purchasedVehicle;
    }

    public EventBooking getEventBooking() {
        return eventBooking;
    }

    public void setEventBooking(EventBooking eventBooking) {
        this.eventBooking = eventBooking;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Generate a unique payment ID
    public static String generatePaymentId() {
        return "PAY-" + System.currentTimeMillis();
    }
}
