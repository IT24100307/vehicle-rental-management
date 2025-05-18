package com.system.project1.service;

import com.system.project1.entity.EventBooking;
import com.system.project1.entity.Payment;
import com.system.project1.entity.PurchasedVehicle;
import com.system.project1.util.FileStorageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private FileStorageManager fileStorageManager;

    // Create a new payment
    public Payment createPayment(Payment payment) {
        // Generate a unique payment ID if not provided
        if (payment.getPaymentId() == null) {
            payment.setPaymentId(Payment.generatePaymentId());
        }

        // Save payment to the file system
        return fileStorageManager.savePayment(payment);
    }

    // Helper method to get all payments
    public List<Payment> getAllPayments() {
        return fileStorageManager.loadAllPayments();
    }

    // Process payment for a rental
    public Payment processPayment(String purchaseId, Payment paymentDetails) {
        try {
            Optional<PurchasedVehicle> optionalPurchasedVehicle = fileStorageService
                    .findPurchasedVehicleByPurchaseId(purchaseId);

            if (optionalPurchasedVehicle.isPresent()) {
                PurchasedVehicle purchasedVehicle = optionalPurchasedVehicle.get();

                // Calculate payment amount based on rental details
                double amount = purchasedVehicle.getVehicle().getRentPrice() * purchasedVehicle.getRentalDays();

                // Update payment details
                paymentDetails.setAmount(amount);
                paymentDetails.setPurchasedVehicle(purchasedVehicle);

                // Process payment logic based on payment method
                if ("CARD".equalsIgnoreCase(paymentDetails.getPaymentMethod())) {
                    // In a real application, you would integrate with a payment gateway here
                    // For now, we'll just mark the payment as completed
                    paymentDetails.setStatus("COMPLETED");
                } else if ("CASH".equalsIgnoreCase(paymentDetails.getPaymentMethod())) {
                    // For cash payments, mark as completed
                    paymentDetails.setStatus("COMPLETED");
                } else {
                    paymentDetails.setStatus("FAILED");
                    return paymentDetails; // Return without saving if payment method is invalid
                }

                // Update the customer's rented vehicles list
                if (purchasedVehicle.getCustomer() != null) {
                    // Make sure the vehicle is marked as rented
                    purchasedVehicle.setRented(true);
                    fileStorageService.savePurchasedVehicle(purchasedVehicle);
                }

                // Save the payment
                return fileStorageManager.savePayment(paymentDetails);
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Process payment for an event booking
    public Payment processEventPayment(String bookingId, Payment paymentDetails) {
        try {
            Optional<EventBooking> optionalBooking = fileStorageService
                    .findEventBookingByBookingId(bookingId);

            if (optionalBooking.isPresent()) {
                EventBooking booking = optionalBooking.get();

                // Set payment amount based on event price
                double amount = booking.getTotalPrice();
                paymentDetails.setAmount(amount);

                // Set booking as the reference instead of a purchased vehicle
                // We'll need to handle this in the file storage manager

                // Process payment based on payment method
                if ("CARD".equalsIgnoreCase(paymentDetails.getPaymentMethod()) ||
                        "CASH".equalsIgnoreCase(paymentDetails.getPaymentMethod())) {
                    paymentDetails.setStatus("COMPLETED");

                    // Update booking status to confirmed
                    booking.setStatus("CONFIRMED");
                    fileStorageService.saveEventBooking(booking);
                } else {
                    paymentDetails.setStatus("FAILED");
                    return paymentDetails;
                }

                // Save the payment
                return fileStorageManager.savePayment(paymentDetails);
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error processing event payment: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Get payment by ID
    public Payment getPaymentById(String paymentId) {
        return getAllPayments().stream()
                .filter(p -> p.getPaymentId().equals(paymentId))
                .findFirst()
                .orElse(null);
    }

    // Get payment for a rental
    public Payment getPaymentForRental(String purchaseId) {
        return getAllPayments().stream()
                .filter(p -> p.getPurchasedVehicle() != null &&
                        p.getPurchasedVehicle().getPurchaseId().equals(purchaseId))
                .findFirst()
                .orElse(null);
    }
}
