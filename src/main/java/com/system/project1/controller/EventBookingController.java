package com.system.project1.controller;

import com.system.project1.entity.EventBooking;
import com.system.project1.service.EventBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/event-bookings")
public class EventBookingController {

    @Autowired
    private EventBookingService eventBookingService;

    // Create a new booking
    @PostMapping
    public ResponseEntity<EventBooking> createBooking(@RequestBody EventBooking booking) {
        EventBooking createdBooking = eventBookingService.createBooking(booking);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    // Create a booking with event and customer IDs
    @PostMapping("/create")
    public ResponseEntity<EventBooking> createBooking(
            @RequestParam Long eventId,
            @RequestParam int customerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date eventDate,
            @RequestParam(required = false) String specialRequirements) {

        EventBooking booking = eventBookingService.createBooking(eventId, customerId, eventDate, specialRequirements);
        if (booking != null) {
            return new ResponseEntity<>(booking, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Get all bookings
    @GetMapping
    public ResponseEntity<List<EventBooking>> getAllBookings() {
        List<EventBooking> bookings = eventBookingService.getAllBookings();
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    // Get booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<EventBooking> getBookingById(@PathVariable Long id) {
        Optional<EventBooking> booking = eventBookingService.getBookingById(id);
        if (booking.isPresent()) {
            return new ResponseEntity<>(booking.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get booking by booking ID
    @GetMapping("/booking-id/{bookingId}")
    public ResponseEntity<EventBooking> getBookingByBookingId(@PathVariable String bookingId) {
        EventBooking booking = eventBookingService.getBookingByBookingId(bookingId);
        if (booking != null) {
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Update a booking
    @PutMapping("/{id}")
    public ResponseEntity<EventBooking> updateBooking(@PathVariable Long id, @RequestBody EventBooking booking) {
        if (!eventBookingService.getBookingById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        booking.setId(id);
        EventBooking updatedBooking = eventBookingService.updateBooking(booking);
        return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
    }

    // Delete a booking
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        if (!eventBookingService.getBookingById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        eventBookingService.deleteBooking(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Get bookings by customer ID
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<EventBooking>> getBookingsByCustomerId(@PathVariable int customerId) {
        List<EventBooking> bookings = eventBookingService.getBookingsByCustomerId(customerId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    // Get bookings by event ID
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventBooking>> getBookingsByEventId(@PathVariable Long eventId) {
        List<EventBooking> bookings = eventBookingService.getBookingsByEventId(eventId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    // Get bookings by date range
    @GetMapping("/date-range")
    public ResponseEntity<List<EventBooking>> getBookingsByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<EventBooking> bookings = eventBookingService.getBookingsByDateRange(startDate, endDate);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    // Get bookings by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventBooking>> getBookingsByStatus(@PathVariable String status) {
        List<EventBooking> bookings = eventBookingService.getBookingsByStatus(status);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    // Change booking status
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<EventBooking> changeBookingStatus(@PathVariable Long id, @PathVariable String status) {
        EventBooking booking = eventBookingService.changeBookingStatus(id, status);
        if (booking != null) {
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
