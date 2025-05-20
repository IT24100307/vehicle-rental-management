package com.system.project1.controller;

import com.system.project1.entity.Payment;
import com.system.project1.entity.PurchasedVehicle;
import com.system.project1.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin("*") // Allow cross-origin requests for easier development
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Process payment for a rental
    @PostMapping("/process/{purchaseId}")
    public ResponseEntity<?> processPayment(@PathVariable String purchaseId, @RequestBody Payment paymentDetails) {
        Payment payment = paymentService.processPayment(purchaseId, paymentDetails);

        if (payment != null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Payment processed successfully",
                            "payment", payment));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Rental not found with ID: " + purchaseId));
        }
    }

    // Process payment for an event booking
    @PostMapping("/process-event/{bookingId}")
    public ResponseEntity<?> processEventPayment(@PathVariable Long bookingId, @RequestBody Payment paymentDetails) {
        Payment payment = paymentService.processEventPayment(bookingId, paymentDetails);

        if (payment != null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Event payment processed successfully",
                            "payment", payment));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event booking not found with ID: " + bookingId));
        }
    }

    // Get payment details
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable String paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);

        if (payment != null) {
            return ResponseEntity.ok(payment);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Payment not found with ID: " + paymentId));
        }
    }

    // Get payment for a rental
    @GetMapping("/rental/{purchaseId}")
    public ResponseEntity<?> getPaymentForRental(@PathVariable String purchaseId) {
        Payment payment = paymentService.getPaymentForRental(purchaseId);

        if (payment != null) {
            return ResponseEntity.ok(payment);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No payment found for rental ID: " + purchaseId));
        }
    }

    // Get payment for an event booking
    @GetMapping("/event/{bookingId}")
    public ResponseEntity<?> getPaymentForEvent(@PathVariable Long bookingId) {
        Payment payment = paymentService.getPaymentForEvent(bookingId);

        if (payment != null) {
            return ResponseEntity.ok(payment);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No payment found for event booking ID: " + bookingId));
        }
    }
}
