package com.system.project1;

import com.system.project1.entity.Event;
import com.system.project1.entity.Vehicle;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventInventory {
    private Event[] events;
    private int eventCount;
    private static final int MAX_EVENTS = 50;
    private static final String IMAGE_DIR = "src/main/resources/static/images/events";

    public EventInventory() {
        events = new Event[MAX_EVENTS];
        eventCount = 0;
        new File(IMAGE_DIR).mkdirs();

        System.out.println("EventInventory initialized with capacity for " + MAX_EVENTS + " events");
    }

    public void addEvent(Event event) {
        if (eventCount < MAX_EVENTS && event != null) {
            // Create a copy of the event to store in memory
            Event storedEvent = copyEvent(event);

            // If the event doesn't have an ID yet, generate a temporary one
            if (storedEvent.getId() == null) {
                // Find the max ID and increment by 1
                long maxId = 0;
                for (int i = 0; i < eventCount; i++) {
                    if (events[i] != null && events[i].getId() != null && events[i].getId() > maxId) {
                        maxId = events[i].getId();
                    }
                }
                storedEvent.setId(maxId + 1);
            }

            // Check if event with same ID already exists
            for (int i = 0; i < eventCount; i++) {
                if (events[i] != null && events[i].getId() != null &&
                        events[i].getId().equals(storedEvent.getId())) {
                    // Update existing event
                    events[i] = storedEvent;
                    System.out.println(
                            "Updated event in memory: " + storedEvent.getName() + " (ID: " + storedEvent.getId() + ")");
                    return;
                }
            }

            // Add as new event
            events[eventCount] = storedEvent;
            eventCount++;
            sortEventsByName();
            System.out
                    .println("Added event to memory: " + storedEvent.getName() + " (ID: " + storedEvent.getId() + ")");
        } else {
            System.out.println("Could not add event: " + (event == null ? "Event is null" : "Max capacity reached"));
        }
    }

    public boolean deleteEvent(Long eventId) {
        for (int i = 0; i < eventCount; i++) {
            if (events[i] != null && events[i].getId() != null && events[i].getId().equals(eventId)) {
                for (int j = i; j < eventCount - 1; j++) {
                    events[j] = events[j + 1];
                }
                events[eventCount - 1] = null;
                eventCount--;
                System.out.println("Deleted event from memory with ID: " + eventId);
                return true;
            }
        }
        System.out.println("Failed to delete event from memory with ID: " + eventId + " (not found)");
        return false;
    }

    public boolean updateEvent(Long eventId, Event updatedEvent) {
        for (int i = 0; i < eventCount; i++) {
            if (events[i] != null && events[i].getId() != null && events[i].getId().equals(eventId)) {
                events[i] = copyEvent(updatedEvent);
                sortEventsByName();
                System.out.println("Updated event in memory: " + updatedEvent.getName() + " (ID: " + eventId + ")");
                return true;
            }
        }
        System.out.println("Failed to update event in memory with ID: " + eventId + " (not found)");
        return false;
    }

    public Event getEventById(Long eventId) {
        for (int i = 0; i < eventCount; i++) {
            if (events[i] != null && events[i].getId() != null && events[i].getId().equals(eventId)) {
                return events[i];
            }
        }
        return null;
    }

    public Event[] getAllEvents() {
        // Create a copy of non-null events
        int nonNullCount = 0;
        for (int i = 0; i < eventCount; i++) {
            if (events[i] != null)
                nonNullCount++;
        }

        Event[] result = new Event[nonNullCount];
        int index = 0;
        for (int i = 0; i < eventCount; i++) {
            if (events[i] != null) {
                result[index++] = events[i];
            }
        }
        return result;
    }

    public int getEventCount() {
        return eventCount;
    }

    // Filter events by type
    public Event[] filterEventsByType(String eventType) {
        if (eventType == null || eventType.trim().isEmpty()) {
            return getAllEvents();
        }

        List<Event> filteredEvents = new ArrayList<>();
        for (int i = 0; i < eventCount; i++) {
            if (events[i] != null && events[i].getEventType() != null &&
                    events[i].getEventType().equals(eventType)) {
                filteredEvents.add(events[i]);
            }

        }

        return filteredEvents.toArray(new Event[0]);
    }

    // Filter events by active status
    public Event[] filterEventsByStatus(boolean active) {
        List<Event> filteredEvents = new ArrayList<>();
        for (int i = 0; i < eventCount; i++) {
            if (events[i] != null && events[i].isActive() == active) {
                filteredEvents.add(events[i]);
            }

        }

        return filteredEvents.toArray(new Event[0]);
    }

    // Sort events by name
    private void sortEventsByName() {
        Arrays.sort(events, 0, eventCount, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                if (e1 == null && e2 == null)
                    return 0;
                if (e1 == null)
                    return 1;
                if (e2 == null)
                    return -1;

                return e1.getName().compareToIgnoreCase(e2.getName());
            }
        });
    }

    // Helper method to create a deep copy of an event
    private Event copyEvent(Event original) {
        if (original == null)
            return null;

        Event copy = new Event();
        copy.setId(original.getId());
        copy.setName(original.getName());
        copy.setDescription(original.getDescription());
        copy.setPrice(original.getPrice());
        copy.setEventType(original.getEventType());
        copy.setDurationHours(original.getDurationHours());
        copy.setImagePath(original.getImagePath());
        copy.setActive(original.isActive());
        copy.setStartDate(original.getStartDate());
        copy.setEndDate(original.getEndDate());

        // Copy vehicles if any
        if (original.getVehicles() != null && !original.getVehicles().isEmpty()) {
            copy.setVehicles(new ArrayList<>(original.getVehicles()));
        }

        return copy;
    }
}