package com.system.project1.controller;

import com.system.project1.entity.Customer;
import com.system.project1.entity.PurchasedVehicle;
import com.system.project1.entity.Vehicle;
import com.system.project1.service.CustomerService;
import com.system.project1.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin("*") // Allow cross-origin requests for easier development
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private VehicleService vehicleService;

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Customer customer = customerService.authenticate(email, password);

        if (customer != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("customer", customer);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }
    }

    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@RequestBody Customer customer) {
        Customer registeredCustomer = customerService.registerCustomer(customer);

        if (registeredCustomer != null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Registration successful",
                            "customer", registeredCustomer));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email already exists"));
        }
    }

    // Get all customers (admin endpoint)
    @GetMapping
    public ResponseEntity<?> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    // Get customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable int id) {
        Customer customer = customerService.getCustomerById(id);

        if (customer != null) {
            return ResponseEntity.ok(customer);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update customer details
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable int id, @RequestBody Customer customer) {
        customer.setCustomerId(id); // Ensure ID is set
        Customer updatedCustomer = customerService.updateCustomer(customer);

        if (updatedCustomer != null) {
            return ResponseEntity.ok(Map.of("message", "Customer updated successfully",
                    "customer", updatedCustomer));
        } else {
            return ResponseEntity.notFound().build();
        }
    } // Delete customer

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable int id) {
        boolean deleted = customerService.deleteCustomer(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Customer deleted successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    } // Rent a vehicle

    @PostMapping("/{customerId}/rent/{vehicleId}")
    public ResponseEntity<?> rentVehicle(
            @PathVariable int customerId,
            @PathVariable String vehicleId,
            @RequestParam int days) {

        System.out.println(
                "Rent vehicle request - customerId: " + customerId + ", vehicleId: " + vehicleId + ", days: " + days);

        try {
            // Debug: Dump all customers
            System.out.println("All customers from DB:");
            List<Customer> allCustomers = customerService.getAllCustomers();
            for (Customer c : allCustomers) {
                System.out.println(
                        "  - ID: " + c.getCustomerId() + ", Name: " + c.getName() + ", Email: " + c.getEmail());
            }

            // Find the vehicle
            Vehicle vehicle = vehicleService.findVehicleById(vehicleId);
            System.out.println("Vehicle found: "
                    + (vehicle != null ? "Yes - " + vehicle.getBrand() + " " + vehicle.getModel() : "No"));

            if (vehicle == null) {
                System.out.println("Vehicle not found with ID: " + vehicleId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Vehicle not found with ID: " + vehicleId));
            }

            // Check if the vehicle already exists in the database, save it if it doesn't
            if (!vehicleService.vehicleExistsInDatabase(vehicleId)) {
                System.out.println("Vehicle not in database, saving it: " + vehicleId);
                vehicle = vehicleService.saveVehicle(vehicle);
            }

            // Find the customer and create rental
            Customer customer = customerService.getCustomerById(customerId);
            System.out.println("Customer found: " + (customer != null ? "Yes - " + customer.getName() : "No"));

            if (customer == null) {
                System.out.println("Customer not found with ID: " + customerId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Customer not found with ID: " + customerId));
            }

            PurchasedVehicle rental = customerService.rentVehicle(customerId, vehicle, days);
            System.out.println("Rental created: " + (rental != null ? "Yes - ID: " + rental.getPurchaseId() : "No"));

            if (rental != null) {
                // Return a simple response with just the purchase ID and message to avoid
                // serialization issues
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of(
                                "message", "Vehicle rented successfully",
                                "purchaseId", rental.getPurchaseId(),
                                "vehicleBrand", vehicle.getBrand(),
                                "vehicleModel", vehicle.getModel(),
                                "days", days));
            } else {
                System.out.println("Failed to create rental");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Failed to create rental"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception when renting vehicle: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error processing rental: " + e.getMessage()));
        }
    }

    // Get customer's rented vehicles

    @GetMapping("/{customerId}/rentals")
    public ResponseEntity<?> getCustomerRentals(@PathVariable int customerId) {
        Customer customer = customerService.getCustomerById(customerId);

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Customer not found with ID: " + customerId));
        }

        List<PurchasedVehicle> rentals = customerService.getCustomerRentals(customerId);
        return ResponseEntity.ok(rentals);
    } // Get a specific rental by ID

    @GetMapping("/rental/{purchaseId}")
    public ResponseEntity<?> getRentalById(@PathVariable String purchaseId) {
        PurchasedVehicle rental = customerService.getRentalById(purchaseId);

        if (rental == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Rental not found with ID: " + purchaseId));
        }

        return ResponseEntity.ok(rental);
    }

    // Return a rented vehicle
    @PostMapping("/rental/{purchaseId}/return")
    public ResponseEntity<?> returnVehicle(@PathVariable String purchaseId) {
        try {
            System.out.println("Return vehicle request - purchaseId: " + purchaseId);

            boolean success = customerService.returnVehicle(purchaseId);

            if (success) {
                // Get the updated purchased vehicle to identify the customer
                PurchasedVehicle rental = customerService.getRentalById(purchaseId);

                // Update customer's rented vehicles list in memory
                if (rental != null && rental.getCustomer() != null) {
                    Customer customer = rental.getCustomer();

                    // Refresh customer data to ensure the frontend gets the latest rented vehicles
                    // list
                    Customer refreshedCustomer = customerService.getCustomerById(customer.getCustomerId());

                    System.out.println("Vehicle returned successfully for customer: " +
                            (refreshedCustomer != null ? refreshedCustomer.getName() : "unknown"));
                }

                return ResponseEntity.ok(Map.of("message", "Vehicle returned successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Failed to return vehicle. Rental not found."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception when returning vehicle: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error processing return: " + e.getMessage()));
        }
    }
}
