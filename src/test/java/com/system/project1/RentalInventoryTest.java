package com.system.project1;

import com.system.project1.entity.Customer;
import com.system.project1.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RentalInventoryTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleInventory vehicleInventory;

    @Test
    public void testRentalRemovesVehicleFromInventory() {
        // Create a test vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleID("TEST-123");
        vehicle.setBrand("Test Brand");
        vehicle.setModel("Test Model");
        vehicle.setRentPrice(100.0);
        vehicle.setRented(false);

        // Add the vehicle to inventory
        vehicleInventory.addVehicle(vehicle);

        // Create a test customer
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setName("Test Customer");
        customer.setEmail("test@example.com");

        // Get the initial count
        int initialCount = vehicleInventory.getVehicleCount();

        // Rent the vehicle
        PurchasedVehicle rental = customerService.rentVehicle(customer.getCustomerId(), vehicle, 5);

        // Verify the vehicle was removed from inventory
        assertEquals(initialCount - 1, vehicleInventory.getVehicleCount(),
                "Vehicle should be removed from inventory after rental");

        // Get all vehicles from inventory
        Vehicle[] vehicles = vehicleInventory.getVehicles();
        boolean vehicleFound = false;

        for (int i = 0; i < vehicleInventory.getVehicleCount(); i++) {
            if (vehicles[i] != null && vehicles[i].getVehicleID().equals("TEST-123")) {
                vehicleFound = true;
                break;
            }
        }

        assertFalse(vehicleFound, "Rented vehicle should not be found in inventory");

        // Now return the vehicle
        if (rental != null) {
            boolean returned = customerService.returnVehicle(rental.getPurchaseId());
            assertTrue(returned, "Return process should succeed");

            // After return, the vehicle should be back in inventory
            assertEquals(initialCount, vehicleInventory.getVehicleCount(),
                    "Vehicle should be added back to inventory after return");

            // Get all vehicles again
            vehicles = vehicleInventory.getVehicles();
            vehicleFound = false;

            for (int i = 0; i < vehicleInventory.getVehicleCount(); i++) {
                if (vehicles[i] != null && vehicles[i].getVehicleID().equals("TEST-123")) {
                    vehicleFound = true;
                    break;
                }
            }

            assertTrue(vehicleFound, "Returned vehicle should be found in inventory");
        }

        // Clean up - remove test vehicle
        vehicleInventory.deleteVehicle("TEST-123");
    }
}
