package com.system.project1.config;

import com.system.project1.entity.Customer;
import com.system.project1.entity.Event;
import com.system.project1.entity.Vehicle;
import com.system.project1.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize customers
        initializeCustomers();

        // Initialize events
        initializeEvents();
    }

    private void initializeCustomers() {
        // Check if we need to add a test customer
        if (fileStorageService.getAllCustomers().isEmpty()) {
            System.out.println("Initializing data with test customer...");

            // Create a test customer
            Customer testCustomer = new Customer(
                    "Test User",
                    "1234567890",
                    12345,
                    "test@example.com",
                    "securepassword");

            // Set the address
            testCustomer.setAddress("123 Test Street, Testville");

            // Save to file
            Customer savedCustomer = fileStorageService.saveCustomer(testCustomer);

            System.out.println("Test customer created with ID: " + savedCustomer.getCustomerId());
        } else {
            System.out.println("Data already has customers, skipping initialization");
        }
    }

    private void initializeEvents() {
        // Don't initialize sample events for production use
        // Check if we need to add sample events
        /*
         * if (fileStorageService.getAllEvents().isEmpty()) {
         * System.out.println("Initializing data with sample events...");
         * 
         * // Create sample events
         * Event weddingEvent = new Event(
         * "Premium Wedding Package",
         * "Luxury transportation for your special day with premium decorated vehicles and professional chauffeurs."
         * ,
         * 1200.00,
         * "WEDDING",
         * 8);
         * weddingEvent.setImagePath("images/wedding-car.jpg");
         * 
         * Event photoshootEvent = new Event(
         * "Vintage Photoshoot Collection",
         * "Classic and vintage vehicles for professional photoshoots, perfect for creating timeless memories."
         * ,
         * 800.00,
         * "PHOTOSHOOT",
         * 4);
         * photoshootEvent.setImagePath("images/vintage-car.jpg");
         * 
         * Event airportEvent = new Event(
         * "VIP Airport Transfer",
         * "Luxury airport pickup and drop-off service with professional drivers and premium vehicles."
         * ,
         * 150.00,
         * "AIRPORT_TRANSFER",
         * 3);
         * airportEvent.setImagePath("images/airport-transfer.jpg");
         * 
         * Event corporateEvent = new Event(
         * "Corporate Fleet Service",
         * "Premium fleet service for corporate events, conferences, and business meetings."
         * ,
         * 500.00,
         * "CORPORATE",
         * 6);
         * corporateEvent.setImagePath("images/corporate-fleet.jpg");
         * 
         * // Save events
         * fileStorageService.saveEvent(weddingEvent);
         * fileStorageService.saveEvent(photoshootEvent);
         * fileStorageService.saveEvent(airportEvent);
         * fileStorageService.saveEvent(corporateEvent);
         * 
         * // Assign random vehicles to events
         * }
         */
    }

    private void assignVehiclesToEvents() {
        // Get all events
        List<Event> events = fileStorageService.getAllEvents();
        if (events.isEmpty()) {
            return;
        }

        // Get all vehicles
        List<Vehicle> vehicles = fileStorageService.getAllVehicles();
        if (vehicles.isEmpty()) {
            return;
        }

        Random random = new Random();

        // Assign random vehicles to each event
        for (Event event : events) {
            // Determine how many vehicles to assign (1-3)
            int numVehicles = random.nextInt(3) + 1;

            for (int i = 0; i < numVehicles && i < vehicles.size(); i++) {
                // Get a random vehicle
                int randomIndex = random.nextInt(vehicles.size());
                Vehicle vehicle = vehicles.get(randomIndex);

                // Add to event and remove from list to avoid duplicates
                event.addVehicle(vehicle);
                vehicles.remove(randomIndex);
            }

            // Save the updated event
            fileStorageService.saveEvent(event);
        }
    }
}
