package com.system.project1.service;

import com.system.project1.entity.Event;
import com.system.project1.entity.Vehicle;
import java.util.List;

public interface EventService {

    /**
     * Get all events
     * 
     * @return List of all events
     */
    List<Event> getAllEvents();

    /**
     * Get event by ID
     * 
     * @param id Event ID
     * @return Event if found, null otherwise
     */
    Event getEventById(Long id);

    /**
     * Save a new event or update an existing one
     * 
     * @param event Event to save
     * @return Saved event
     */
    Event saveEvent(Event event);

    /**
     * Delete an event
     * 
     * @param id Event ID
     */
    void deleteEvent(Long id);

    /**
     * Get all active events
     * 
     * @return List of active events
     */
    List<Event> getActiveEvents();

    /**
     * Get events by type
     * 
     * @param eventType Event type
     * @return List of events of the specified type
     */
    List<Event> getEventsByType(String eventType);
}
