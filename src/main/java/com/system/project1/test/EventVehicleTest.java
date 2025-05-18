package com.system.project1.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class to verify that the vehicle-event association works correctly.
 * This test can be run as part of the regular Spring Boot application startup
 * by passing the argument "run-event-vehicle-test".
 * 
 * In normal application startup, this test will be skipped.
 */
@Component
public class EventVehicleTest implements CommandLineRunner {

    @Autowired
    private EventService eventService;

    @Autowired
    private VehicleService vehicleService;

    @Override
    public void run(String... args) throws Exception {
        // Skip running this test unless explicitly requested
        if (args.length > 0 && args[0].equals("run-event-vehicle-test")) {
            testEventVehicleAssociation();
        }
    }

    private void testEventVehicleAssociation() {
        try {
            System.out.println("\n=== Running Event-Vehicle Association Test ===\n");

            // Get all available vehicles
            List<Vehicle> allVehicles = vehicleService.getAllVehicles();

            if (allVehicles.isEmpty()) {
                System.out.println("No vehicles found in database. Test cannot continue.");
                return;
            }

            // Take first two vehicles for our test
            Vehicle vehicle1 = allVehicles.get(0);
            Vehicle vehicle2 = allVehicles.size() > 1 ? allVehicles.get(1) : allVehicles.get(0);

            System.out
                    .println("Selected test vehicles: " + vehicle1.getVehicleID() + " and " + vehicle2.getVehicleID());

            // Create a test event with these vehicles
            Event testEvent = new Event();
            testEvent.setName("Test Event for Vehicle Association");
            testEvent.setDescription("This is a test event to verify vehicle association");
            testEvent.setPrice(500.0);
            testEvent.setEventType("TEST");
            testEvent.setDurationHours(2);
            testEvent.setStartDate(new Date());
            testEvent.setEndDate(new Date());
            testEvent.setActive(true);

            // Add vehicle IDs to the event
            List<String> vehicleIds = new ArrayList<>();
            vehicleIds.add(vehicle1.getVehicleID());
            vehicleIds.add(vehicle2.getVehicleID());
            testEvent.setVehicleIds(vehicleIds);

            // Save the event
            Event savedEvent = eventService.saveEvent(testEvent);
            System.out.println("Created test event with ID: " + savedEvent.getId());

            // Check if the vehicles were properly associated
            System.out.println("Testing event->vehicle association...");
            Event retrievedEvent = eventService.getEventById(savedEvent.getId());

            if (retrievedEvent != null) {
                // Check vehicle IDs
                System.out.println("Retrieved event has vehicleIds: " +
                        (retrievedEvent.getVehicleIds() != null ? retrievedEvent.getVehicleIds().size() : "null"));

                // Check vehicle objects
                System.out.println("Retrieved event has vehicles: " +
                        (retrievedEvent.getVehicles() != null ? retrievedEvent.getVehicles().size() : "null"));

                boolean success = true;

                // Verify vehicle IDs are present
                if (retrievedEvent.getVehicleIds() == null || retrievedEvent.getVehicleIds().size() != 2) {
                    System.out.println("ERROR: Expected 2 vehicle IDs, but found " +
                            (retrievedEvent.getVehicleIds() != null ? retrievedEvent.getVehicleIds().size() : "null"));
                    success = false;
                } else {
                    System.out.println("Vehicle IDs correctly saved with event.");
                }

                // Verify vehicle objects are present
                if (retrievedEvent.getVehicles() == null || retrievedEvent.getVehicles().size() != 2) {
                    System.out.println("ERROR: Expected 2 vehicle objects, but found " +
                            (retrievedEvent.getVehicles() != null ? retrievedEvent.getVehicles().size() : "null"));
                    success = false;
                } else {
                    System.out.println("Vehicle objects correctly associated with event.");
                }

                // Clean up - delete the test event
                eventService.deleteEvent(retrievedEvent.getId());
                System.out.println("Deleted test event.");

                if (success) {
                    System.out.println("\n✅ TEST PASSED: Vehicles are correctly saved with events.\n");
                } else {
                    System.out.println("\n❌ TEST FAILED: Issue detected with vehicle-event association.\n");
                }
            } else {
                System.out.println("ERROR: Could not retrieve the saved event!");
            }

        } catch (Exception e) {
            System.out.println("Exception during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
