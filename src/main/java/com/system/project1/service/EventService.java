package com.system.project1.service;

import com.system.project1.entity.Event;
import com.system.project1.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(String eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        return event.orElse(null);
    }

    public List<Event> getEventsByType(String eventType) {
        return eventRepository.findByEventType(eventType);
    }

    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public void deleteEvent(String eventId) {
        eventRepository.deleteById(eventId);
    }

    public Event updateEvent(String eventId, Event eventDetails) {
        Event existingEvent = getEventById(eventId);
        if (existingEvent != null) {
            existingEvent.setEventType(eventDetails.getEventType());
            existingEvent.setBasePrice(eventDetails.getBasePrice());
            existingEvent.setVehicleTypes(eventDetails.getVehicleTypes());
            return eventRepository.save(existingEvent);
        }
        return null;
    }
}