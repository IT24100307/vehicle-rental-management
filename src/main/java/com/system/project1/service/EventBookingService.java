package com.system.project1.service;

import com.system.project1.entity.Customer;
import com.system.project1.entity.Event;
import com.system.project1.entity.EventBooking;
import com.system.project1.entity.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventBookingService {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PaymentService paymentService;

    // Create a new booking
    public EventBooking createBooking(EventBooking booking) {
        return fileStorageService.saveEventBooking(booking);
    }

    // Create a booking with event and customer IDs
    public EventBooking createBooking(Long eventId, int customerId, Date eventDate, String specialRequirements) {
        Optional<Event> eventOpt = fileStorageService.findEventById(eventId);
        Optional<Customer> customerOpt = fileStorageService.findCustomerById(customerId);

        if (eventOpt.isPresent() && customerOpt.isPresent()) {
            Event event = eventOpt.get();
            Customer customer = customerOpt.get();

            EventBooking booking = new EventBooking(event, customer, eventDate);
            booking.setSpecialRequirements(specialRequirements);

            // Add the booking to the customer's bookings
            if (customer.getEventBookings() != null) {
                customer.getEventBookings().add(booking);
                fileStorageService.saveCustomer(customer);
            }

            return fileStorageService.saveEventBooking(booking);
        }
        return null;
    }

    // Buy an event - create booking and process payment
    public EventBooking buyEvent(Long eventId, int customerId, Date eventDate, String specialRequirements,
            String paymentMethod, String cardHolderName, String cardNumber, String cardExpiry) {
        try {
            // Create booking first
            EventBooking booking = createBooking(eventId, customerId, eventDate, specialRequirements);

            if (booking != null) {
                // Create payment object
                Payment payment = new Payment();
                payment.setPaymentId(Payment.generatePaymentId());
                payment.setPaymentMethod(paymentMethod);
                payment.setStatus("PENDING");

                // Set card details if paying by card
                if ("CARD".equalsIgnoreCase(paymentMethod)) {
                    payment.setCardHolderName(cardHolderName);
                    payment.setCardNumber(cardNumber);
                    payment.setCardExpiry(cardExpiry);
                }

                // Process the payment
                Payment completedPayment = paymentService.processEventPayment(booking.getBookingId(), payment);

                if (completedPayment != null && "COMPLETED".equals(completedPayment.getStatus())) {
                    // Payment successful, confirm the booking
                    booking.setStatus("CONFIRMED");
                    return fileStorageService.saveEventBooking(booking);
                } else {
                    // Payment failed, mark booking as payment pending
                    booking.setStatus("PAYMENT_PENDING");
                    return fileStorageService.saveEventBooking(booking);
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error buying event: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Get all bookings
    public List<EventBooking> getAllBookings() {
        return fileStorageService.getAllEventBookings();
    }

    // Get booking by ID
    public Optional<EventBooking> getBookingById(Long id) {
        return fileStorageService.findEventBookingById(id);
    }

    // Get booking by booking ID
    public EventBooking getBookingByBookingId(String bookingId) {
        return fileStorageService.findEventBookingByBookingId(bookingId).orElse(null);
    }

    // Update a booking
    public EventBooking updateBooking(EventBooking booking) {
        return fileStorageService.saveEventBooking(booking);
    }

    // Delete a booking
    public void deleteBooking(Long id) {
        List<EventBooking> bookings = fileStorageService.getAllEventBookings();
        bookings.removeIf(b -> b.getId().equals(id));
        for (EventBooking b : bookings) {
            fileStorageService.saveEventBooking(b);
        }
    }

    // Get bookings by customer
    public List<EventBooking> getBookingsByCustomer(Customer customer) {
        return fileStorageService.getAllEventBookings().stream()
                .filter(b -> b.getCustomer() != null &&
                        b.getCustomer().getCustomerId() == customer.getCustomerId())
                .collect(Collectors.toList());
    }

    // Get bookings by customer ID
    public List<EventBooking> getBookingsByCustomerId(int customerId) {
        return fileStorageService.findEventBookingsByCustomerId(customerId);
    }

    // Get bookings by event
    public List<EventBooking> getBookingsByEventId(Long eventId) {
        return fileStorageService.getAllEventBookings().stream()
                .filter(b -> b.getEvent() != null && b.getEvent().getId().equals(eventId))
                .collect(Collectors.toList());
    }

    // Get bookings by date range
    public List<EventBooking> getBookingsByDateRange(Date startDate, Date endDate) {
        return fileStorageService.getAllEventBookings().stream()
                .filter(b -> b.getEventDate() != null &&
                        !b.getEventDate().before(startDate) &&
                        !b.getEventDate().after(endDate))
                .collect(Collectors.toList());
    }

    // Get bookings by status
    public List<EventBooking> getBookingsByStatus(String status) {
        return fileStorageService.getAllEventBookings().stream()
                .filter(b -> status.equals(b.getStatus()))
                .collect(Collectors.toList());
    }

    // Change booking status
    public EventBooking changeBookingStatus(Long id, String status) {
        Optional<EventBooking> bookingOpt = fileStorageService.findEventBookingById(id);

        if (bookingOpt.isPresent()) {
            EventBooking booking = bookingOpt.get();
            booking.setStatus(status);
            return fileStorageService.saveEventBooking(booking);
        }
        return null;
    }
}
