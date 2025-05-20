package com.system.project1.controller;

import com.system.project1.entity.Customer;
import com.system.project1.entity.Event;
import com.system.project1.entity.EventBooking;
import com.system.project1.service.EventBookingService;
import com.system.project1.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/event-bookings")
public class EventBookingController {

    @Autowired
    private EventBookingService eventBookingService;

    @Autowired
    private FileStorageService fileStorageService;

    // Create a new booking
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> request) {
        try {
            // Extract event and customer IDs from the request
            Long eventId = null;
            Integer customerId = null;

            // Check if event is present in the request
            if (request.containsKey("event") && request.get("event") instanceof Map) {
                Map<String, Object> eventMap = (Map<String, Object>) request.get("event");
                if (eventMap.containsKey("id")) {
                    Object idObj = eventMap.get("id");
                    // Handle both Integer and Double representations of ID
                    if (idObj instanceof Integer) {
                        eventId = ((Integer) idObj).longValue();
                    } else if (idObj instanceof Double) {
                        eventId = ((Double) idObj).longValue();
                    } else if (idObj instanceof Long) {
                        eventId = (Long) idObj;
                    } else if (idObj instanceof String) {
                        eventId = Long.parseLong((String) idObj);
                    }
                }
            }

            // Check if customer is present in the request
            if (request.containsKey("customer") && request.get("customer") instanceof Map) {
                Map<String, Object> customerMap = (Map<String, Object>) request.get("customer");
                if (customerMap.containsKey("id")) {
                    Object idObj = customerMap.get("id");
                    if (idObj instanceof Integer) {
                        customerId = (Integer) idObj;
                    } else if (idObj instanceof Double) {
                        customerId = ((Double) idObj).intValue();
                    } else if (idObj instanceof String) {
                        customerId = Integer.parseInt((String) idObj);
                    }
                }
            }

            // Extract other required fields
            String bookingDateStr = (String) request.get("bookingDate");
            String specialRequests = (String) request.get("specialRequests");

            // Validate required fields
            if (eventId == null || customerId == null || bookingDateStr == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing required fields: eventId, customerId, or bookingDate"));
            }

            // Parse the booking date
            Date bookingDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(bookingDateStr);

            // Create the booking
            EventBooking booking = eventBookingService.createBooking(eventId, customerId, bookingDate, specialRequests);

            if (booking != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(booking);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Failed to create booking. Event or customer not found."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create booking: " + e.getMessage()));
        }
    }

    // Create a booking with event and customer IDs (using request parameters)
    @PostMapping("/create")
    public ResponseEntity<EventBooking> createBookingWithParams(
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
