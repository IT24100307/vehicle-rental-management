package com.system.project1.service;

import com.system.project1.EventInventory;
import com.system.project1.entity.Event;
import com.system.project1.entity.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private EventInventory eventInventory;

    @Override
    public List<Event> getAllEvents() {
        try {
            // First try to get from file storage
            List<Event> storageEvents = fileStorageService.getAllEvents();

            if (storageEvents != null && !storageEvents.isEmpty()) {
                System.out.println("Retrieved " + storageEvents.size() + " events from file storage");
                return storageEvents;
            }

            // If file storage retrieval fails or returns empty, use in-memory inventory
            Event[] inventoryEvents = eventInventory.getAllEvents();
            if (inventoryEvents != null && inventoryEvents.length > 0) {
                List<Event> events = Arrays.asList(inventoryEvents);
                System.out.println("Retrieved " + events.size() + " events from in-memory inventory");
                return events;
            }

            // If both fail, return empty list
            System.out.println("No events found in file storage or in-memory inventory");
            return new ArrayList<>();
        } catch (Exception e) {
            System.out.println("Error retrieving events from file storage: " + e.getMessage());

            // Fallback to in-memory inventory
            Event[] inventoryEvents = eventInventory.getAllEvents();
            if (inventoryEvents != null && inventoryEvents.length > 0) {
                List<Event> events = Arrays.asList(inventoryEvents);
                System.out.println("Fallback: Retrieved " + events.size() + " events from in-memory inventory");
                return events;
            }

            return new ArrayList<>();
        }
    }

    @Override
    public Event getEventById(Long id) {
        try {
            // First try to find from file storage
            Optional<Event> eventOpt = fileStorageService.findEventById(id);
            if (eventOpt.isPresent()) {
                System.out.println("Retrieved event with ID " + id + " from file storage");
                return eventOpt.get();
            }

            // If not found in file storage, check EventInventory
            Event inventoryEvent = eventInventory.getEventById(id);
            if (inventoryEvent != null) {
                System.out.println("Retrieved event with ID " + id + " from in-memory inventory");
                return inventoryEvent;
            }

            System.out.println("Event with ID " + id + " not found in file storage or in-memory inventory");
            return null;
        } catch (Exception e) {
            System.out.println("Error retrieving event with ID " + id + " from file storage: " + e.getMessage());

            // Fallback to in-memory inventory
            Event inventoryEvent = eventInventory.getEventById(id);
            if (inventoryEvent != null) {
                System.out.println("Fallback: Retrieved event with ID " + id + " from in-memory inventory");
                return inventoryEvent;
            }

            return null;
        }
    }

    @Override
    public Event saveEvent(Event event) {
        try {
            // Process vehicle IDs if provided
            if (event.getVehicleIds() != null && !event.getVehicleIds().isEmpty()) {
                List<Vehicle> vehicles = new ArrayList<>();

                for (String vehicleId : event.getVehicleIds()) {
                    if (vehicleId == null || vehicleId.trim().isEmpty()) {
                        System.out.println("Warning: Null or empty vehicle ID found, skipping");
                        continue;
                    }

                    try {
                        Vehicle vehicle = vehicleService.findVehicleById(vehicleId);
                        if (vehicle != null) {
                            // Avoid adding duplicate vehicles
                            boolean alreadyAdded = false;
                            for (Vehicle existingVehicle : vehicles) {
                                if (existingVehicle.getVehicleID().equals(vehicle.getVehicleID())) {
                                    alreadyAdded = true;
                                    break;
                                }
                            }

                            if (!alreadyAdded) {
                                // For inventory vehicles that aren't in the file storage yet, save them first
                                if (!vehicleService.vehicleExistsInDatabase(vehicle.getVehicleID())) {
                                    System.out.println("Vehicle with ID " + vehicleId
                                            + " is from inventory and needs to be saved to file storage");
                                    vehicle = vehicleService.saveVehicle(vehicle);
                                    System.out.println("Saved vehicle to file storage: " + vehicle.getVehicleID());
                                }

                                vehicles.add(vehicle);
                                System.out.println("Added vehicle with ID " + vehicleId + " to event");
                            } else {
                                System.out.println("Vehicle with ID " + vehicleId
                                        + " already added to this event, skipping duplicate");
                            }
                        } else {
                            System.out.println("Warning: Vehicle with ID " + vehicleId + " not found, skipping");
                        }
                    } catch (Exception e) {
                        System.out.println("Error processing vehicle with ID " + vehicleId + ": " + e.getMessage());
                    }
                }

                // Update event with vehicles only if we found some
                if (!vehicles.isEmpty()) {
                    event.setVehicles(vehicles);
                    System.out.println("Total vehicles added to event: " + vehicles.size());
                } else {
                    System.out.println("No valid vehicles found from the provided IDs");
                }
            }

            System.out.println("About to save event to file storage: " + event);
            System.out
                    .println("Event vehicles: " + (event.getVehicles() != null ? event.getVehicles().size() : "null"));

            // Log more details about the event before saving
            System.out.println("Event ID: " + event.getId());
            System.out.println("Event name: " + event.getName());
            System.out.println("Event type: " + event.getEventType());
            System.out.println("Start date: " + event.getStartDate());
            System.out.println("End date: " + event.getEndDate());
            System.out.println("Description: " + (event.getDescription() != null
                    ? event.getDescription().substring(0, Math.min(event.getDescription().length(), 50)) + "..."
                    : "null"));

            // Check for required fields before saving
            if (event.getName() == null || event.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Event name is required");
            }

            if (event.getStartDate() == null) {
                throw new IllegalArgumentException("Start date is required");
            }

            if (event.getEndDate() == null) {
                throw new IllegalArgumentException("End date is required");
            }

            Event savedEvent = null;
            boolean savedToFileStorage = false;

            try {
                // If it's a new event, file storage service will assign an ID
                savedEvent = fileStorageService.saveEvent(event);
                savedToFileStorage = true;
                System.out.println("Successfully saved event to file storage with ID: " + savedEvent.getId());
            } catch (Exception e) {
                System.out.println("Error saving event to file storage: " + e.getMessage());
                e.printStackTrace();

                // If we couldn't save to the file storage but have a valid event, use it
                savedEvent = event;
            }

            // Also save to in-memory inventory for redundancy
            if (savedEvent != null) {
                eventInventory.addEvent(savedEvent);
                System.out.println("Saved event to in-memory inventory: " + savedEvent.getName());
            } else if (!savedToFileStorage) {
                // If we have no saved event and file storage save failed, create a temporary
                // one
                System.out.println("Creating temporary event in memory since file storage save failed");
                eventInventory.addEvent(event);
                savedEvent = event;
            }

            return savedEvent;
        } catch (Exception e) {
            System.out.println("Error saving event: " + e.getMessage());
            e.printStackTrace();

            // Try to save in memory as fallback
            System.out.println("Attempting to save event to in-memory inventory as fallback");
            eventInventory.addEvent(event);

            throw new RuntimeException("Unable to save event: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEvent(Long id) {
        try {
            // Delete from file storage
            boolean removedFromStorage = fileStorageService.deleteEvent(id);

            // Also remove from in-memory inventory
            boolean removedFromInventory = eventInventory.deleteEvent(id);

            if (removedFromStorage || removedFromInventory) {
                System.out.println("Deleted event with ID " + id);
            } else {
                System.out.println("Event with ID " + id + " not found in either storage mechanism");
            }
        } catch (Exception e) {
            System.out.println("Error deleting event with ID " + id + ": " + e.getMessage());
        }
    }

    @Override
    public List<Event> getActiveEvents() {
        try {
            List<Event> activeEvents = fileStorageService.getAllEvents().stream()
                    .filter(Event::isActive)
                    .collect(Collectors.toList());
            System.out.println("Retrieved " + activeEvents.size() + " active events from file storage");
            return activeEvents;
        } catch (Exception e) {
            System.out.println("Error retrieving active events from file storage: " + e.getMessage());

            // Fallback to in-memory inventory
            Event[] inventoryEvents = eventInventory.getAllEvents();
            List<Event> activeEvents = Arrays.stream(inventoryEvents)
                    .filter(Event::isActive)
                    .collect(Collectors.toList());
            System.out
                    .println("Fallback: Retrieved " + activeEvents.size() + " active events from in-memory inventory");
            return activeEvents;
        }
    }

    @Override
    public List<Event> getEventsByType(String eventType) {
        try {
            List<Event> typedEvents = fileStorageService.findEventsByType(eventType);
            System.out
                    .println("Retrieved " + typedEvents.size() + " events of type " + eventType + " from file storage");
            return typedEvents;
        } catch (Exception e) {
            System.out
                    .println("Error retrieving events of type " + eventType + " from file storage: " + e.getMessage());

            // Fallback to in-memory inventory
            Event[] filteredEvents = eventInventory.filterEventsByType(eventType);
            List<Event> typedEvents = Arrays.asList(filteredEvents);

            System.out.println("Fallback: Retrieved " + typedEvents.size() + " events of type " + eventType
                    + " from in-memory inventory");
            return typedEvents;
        }
    }

    // Add vehicle to event
    public Event addVehicleToEvent(Long eventId, String vehicleId) {
        Event event = getEventById(eventId);
        Vehicle vehicle = vehicleService.findVehicleById(vehicleId);

        if (event != null && vehicle != null) {
            boolean vehicleAlreadyAdded = event.getVehicles().contains(vehicle);

            if (!vehicleAlreadyAdded) {
                event.getVehicles().add(vehicle);

                // Try to save to file storage
                Event savedEvent = null;
                try {
                    savedEvent = fileStorageService.saveEvent(event);
                    System.out.println("Added vehicle " + vehicleId + " to event " + eventId + " in file storage");
                } catch (Exception e) {
                    System.out
                            .println("Error adding vehicle " + vehicleId + " to event " + eventId + " in file storage: "
                                    + e.getMessage());
                    savedEvent = event; // Use the unsaved event if file storage save fails
                }

                // Also update in memory
                eventInventory.updateEvent(eventId, savedEvent != null ? savedEvent : event);
                System.out.println("Added vehicle " + vehicleId + " to event " + eventId + " in memory");

                return savedEvent != null ? savedEvent : event;
            }
        }
        return null;
    }

    // Remove vehicle from event
    public Event removeVehicleFromEvent(Long eventId, String vehicleId) {
        Event event = getEventById(eventId);
        Vehicle vehicle = vehicleService.findVehicleById(vehicleId);

        if (event != null && vehicle != null) {
            boolean removed = event.getVehicles().remove(vehicle);

            if (removed) {
                // Try to save to file storage
                Event savedEvent = null;
                try {
                    savedEvent = fileStorageService.saveEvent(event);
                    System.out.println("Removed vehicle " + vehicleId + " from event " + eventId + " in file storage");
                } catch (Exception e) {
                    System.out.println("Error removing vehicle " + vehicleId + " from event " + eventId
                            + " in file storage: " + e.getMessage());
                    savedEvent = event; // Use the unsaved event if file storage save fails
                }

                // Also update in memory
                eventInventory.updateEvent(eventId, savedEvent != null ? savedEvent : event);
                System.out.println("Removed vehicle " + vehicleId + " from event " + eventId + " in memory");

                return savedEvent != null ? savedEvent : event;
            }
        }
        return null;
    }

    // Get available vehicles that are not already assigned to the event
    public List<Vehicle> getAvailableVehiclesForEvent(Long eventId) {
        List<Vehicle> allVehicles = vehicleService.getAllVehicles();
        Event event = getEventById(eventId);

        if (event != null && event.getVehicles() != null) {
            // Filter out vehicles that are already assigned to this event
            return allVehicles.stream()
                    .filter(v -> event.getVehicles().stream()
                            .noneMatch(ev -> ev.getVehicleID().equals(v.getVehicleID())))
                    .collect(Collectors.toList());
        }

        return allVehicles;
    }
}