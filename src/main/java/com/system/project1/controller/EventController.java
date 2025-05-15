package com.system.project1.controller;

import com.system.project1.entity.AirportTransferEvent;
import com.system.project1.entity.Event;
import com.system.project1.entity.LongTripEvent;
import com.system.project1.entity.WeddingPackageEvent;
import com.system.project1.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEventById(@PathVariable String eventId) {
        Event event = eventService.getEventById(eventId);
        if (event != null) {
            return ResponseEntity.ok(event);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<Event>> getEventsByType(@PathVariable String eventType) {
        return ResponseEntity.ok(eventService.getEventsByType(eventType));
    }

    @PostMapping("/airport-transfer")
    public ResponseEntity<AirportTransferEvent> createAirportTransferEvent(@RequestBody AirportTransferEvent event) {
        // Generate a unique ID if not provided
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            event.setEventId("APT-" + UUID.randomUUID().toString().substring(0, 8));
        }

        // Set scheduled date if not provided
        if (event.getScheduledDate() == null) {
            event.setScheduledDate(LocalDateTime.now().plusDays(1));
        }

        return new ResponseEntity<>(
                (AirportTransferEvent) eventService.saveEvent(event),
                HttpStatus.CREATED);
    }

    @PostMapping("/long-trip")
    public ResponseEntity<LongTripEvent> createLongTripEvent(@RequestBody LongTripEvent event) {
        // Generate a unique ID if not provided
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            event.setEventId("LT-" + UUID.randomUUID().toString().substring(0, 8));
        }

        // Set scheduled date if not provided
        if (event.getScheduledDate() == null) {
            event.setScheduledDate(LocalDateTime.now().plusDays(1));
        }

        return new ResponseEntity<>(
                (LongTripEvent) eventService.saveEvent(event),
                HttpStatus.CREATED);
    }

    @PostMapping("/wedding-package")
    public ResponseEntity<WeddingPackageEvent> createWeddingPackageEvent(@RequestBody WeddingPackageEvent event) {
        // Generate a unique ID if not provided
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            event.setEventId("WP-" + UUID.randomUUID().toString().substring(0, 8));
        }

        // Set scheduled date if not provided
        if (event.getScheduledDate() == null) {
            event.setScheduledDate(LocalDateTime.now().plusDays(7)); // Wedding usually booked well in advance
        }

        return new ResponseEntity<>(
                (WeddingPackageEvent) eventService.saveEvent(event),
                HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        // Generate a unique ID if not provided
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            String prefix = "EVT-";
            event.setEventId(prefix + UUID.randomUUID().toString().substring(0, 8));
        }

        // Set scheduled date if not provided
        if (event.getScheduledDate() == null) {
            event.setScheduledDate(LocalDateTime.now().plusDays(1));
        }

        return new ResponseEntity<>(
                eventService.saveEvent(event),
                HttpStatus.CREATED);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<Event> updateEvent(@PathVariable String eventId, @RequestBody Event eventDetails) {
        Event updatedEvent = eventService.updateEvent(eventId, eventDetails);
        if (updatedEvent != null) {
            return ResponseEntity.ok(updatedEvent);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{eventId}/status/{status}")
    public ResponseEntity<Event> updateEventStatus(@PathVariable String eventId, @PathVariable String status) {
        Event event = eventService.getEventById(eventId);
        if (event != null) {
            event.setStatus(status.toUpperCase());
            return ResponseEntity.ok(eventService.saveEvent(event));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String eventId) {
        Event event = eventService.getEventById(eventId);
        if (event != null) {
            eventService.deleteEvent(eventId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{eventId}/price")
    public ResponseEntity<Double> calculateEventPrice(@PathVariable String eventId) {
        Event event = eventService.getEventById(eventId);
        if (event != null) {
            return ResponseEntity.ok(event.calculateTotalPrice());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
