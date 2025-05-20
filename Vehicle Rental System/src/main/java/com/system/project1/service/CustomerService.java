package com.system.project1.service;

import com.system.project1.VehicleInventory;
import com.system.project1.entity.Customer;
import com.system.project1.entity.PurchasedVehicle;
import com.system.project1.entity.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private VehicleInventory vehicleInventory;

    // User authentication
    public Customer authenticate(String email, String password) {
        Optional<Customer> optionalCustomer = fileStorageService.findCustomerByEmail(email);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            if (customer.getPassword().equals(password)) {
                return customer;
            }
        }
        return null;
    }

    // Register a new customer
    public Customer registerCustomer(Customer customer) {
        // Check if email already exists
        if (fileStorageService.findCustomerByEmail(customer.getEmail()).isPresent()) {
            return null; // Email already exists
        }

        return fileStorageService.saveCustomer(customer);
    }

    // Update customer details
    public Customer updateCustomer(Customer customer) {
        if (fileStorageService.findCustomerById(customer.getCustomerId()).isPresent()) {
            return fileStorageService.saveCustomer(customer);
        }
        return null;
    }

    // Get customer by ID
    public Customer getCustomerById(int id) {
        return fileStorageService.findCustomerById(id).orElse(null);
    }

    // Get customer by email
    public Customer getCustomerByEmail(String email) {
        return fileStorageService.findCustomerByEmail(email).orElse(null);
    }

    // Get all customers
    public List<Customer> getAllCustomers() {
        return fileStorageService.getAllCustomers();
    }

    // Delete customer
    public boolean deleteCustomer(int id) {
        Optional<Customer> customer = fileStorageService.findCustomerById(id);
        if (customer.isPresent()) {
            List<Customer> customers = fileStorageService.getAllCustomers();
            customers.removeIf(c -> c.getCustomerId() == id);
            for (Customer c : customers) {
                fileStorageService.saveCustomer(c);
            }
            return true;
        }
        return false;
    }

    // Rent a vehicle for a customer
    public PurchasedVehicle rentVehicle(int customerId, Vehicle vehicle, int days) {
        try {
            Optional<Customer> optionalCustomer = fileStorageService.findCustomerById(customerId);

            if (optionalCustomer.isPresent()) {
                Customer customer = optionalCustomer.get();

                // Make a complete deep copy of the vehicle to preserve all properties
                Vehicle vehicleCopy = createDeepCopy(vehicle);

                // Mark the vehicle as rented in the inventory instead of removing it
                vehicle.setRented(true);
                vehicleInventory.updateVehicle(vehicle.getVehicleID(), vehicle);

                // Update the vehicle in file storage as rented
                Optional<Vehicle> storedVehicle = fileStorageService.findVehicleById(vehicle.getVehicleID());
                if (storedVehicle.isPresent()) {
                    Vehicle storedVehicleObj = storedVehicle.get();
                    storedVehicleObj.setRented(true);
                    fileStorageService.saveVehicle(storedVehicleObj);
                    System.out.println("Vehicle marked as rented in file storage: " + vehicle.getVehicleID());
                } else {
                    // If the vehicle doesn't exist in file storage, save it as a new entry
                    Vehicle newVehicleEntry = createDeepCopy(vehicle);
                    newVehicleEntry.setRented(true);
                    fileStorageService.saveVehicle(newVehicleEntry);
                    System.out.println("New rented vehicle saved to file storage: " + vehicle.getVehicleID());
                }

                // Create the rental object with the copied vehicle
                PurchasedVehicle purchasedVehicle = new PurchasedVehicle(vehicleCopy, days, customer);

                // Ensure all required fields are set
                if (purchasedVehicle.getPurchaseId() == null) {
                    purchasedVehicle.setPurchaseId("P" + System.currentTimeMillis());
                }

                if (purchasedVehicle.getCustomerName() == null && customer.getName() != null) {
                    purchasedVehicle.setCustomerName(customer.getName());
                }

                if (purchasedVehicle.getContactNumber() == null && customer.getContactNumber() != null) {
                    purchasedVehicle.setContactNumber(customer.getContactNumber());
                }

                // Mark the rental as rented
                purchasedVehicle.setRented(true);

                // Add to customer's rented vehicles list if not already present
                if (customer.getRentedVehiclesList() == null) {
                    customer.setRentedVehiclesList(new ArrayList<>());
                }

                // Check if this vehicle is already in the customer's rented list
                boolean alreadyRented = customer.getRentedVehiclesList().stream()
                        .anyMatch(pv -> pv.getVehicle().getVehicleID().equals(vehicle.getVehicleID()));

                if (!alreadyRented) {
                    customer.getRentedVehiclesList().add(purchasedVehicle);
                    System.out.println("Added vehicle to customer's rented list: " + vehicle.getVehicleID());
                }

                // Save the updated customer
                fileStorageService.saveCustomer(customer);

                // Save and return the rental
                System.out.println("Saving rental with purchase ID: " + purchasedVehicle.getPurchaseId());
                return fileStorageService.savePurchasedVehicle(purchasedVehicle);
            } else {
                System.out.println("Customer not found with ID: " + customerId);
            }
        } catch (Exception e) {
            System.out.println("Error in rentVehicle: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Create a deep copy of a vehicle preserving all properties including vehicle
    // type and specific properties
    private Vehicle createDeepCopy(Vehicle original) {
        Vehicle copy = new Vehicle();
        copy.setVehicleID(original.getVehicleID());
        copy.setBrand(original.getBrand());
        copy.setModel(original.getModel());
        copy.setRentPrice(original.getRentPrice());
        copy.setImagePath(original.getImagePath());

        // Try to copy vehicle type property if it exists
        try {
            Integer vehicleType = (Integer) original.getClass().getMethod("getVehicleType").invoke(original);
            if (vehicleType != null) {
                copy.getClass().getMethod("setVehicleType", int.class).invoke(copy, vehicleType);
            }
        } catch (Exception e) {
            System.out.println("Could not copy vehicle type: " + e.getMessage());
        }

        // Try to copy any vehicle subclass specific properties
        copyPropertyIfExists(original, copy, "getNumberOfDoors", "setNumberOfDoors", int.class);
        copyPropertyIfExists(original, copy, "getTransmissionType", "setTransmissionType", String.class);
        copyPropertyIfExists(original, copy, "getCargoCapacity", "setCargoCapacity", double.class);
        copyPropertyIfExists(original, copy, "getEngineCapacity", "setEngineCapacity", int.class);
        copyPropertyIfExists(original, copy, "getSeatingCapacity", "setSeatingCapacity", int.class);
        copyPropertyIfExists(original, copy, "getMaxLoad", "setMaxLoad", double.class);

        return copy;
    }

    // Helper method to copy a property using reflection
    private void copyPropertyIfExists(Vehicle source, Vehicle target, String getterName, String setterName,
            Class<?> paramType) {
        try {
            Object value = source.getClass().getMethod(getterName).invoke(source);
            if (value != null) {
                target.getClass().getMethod(setterName, paramType).invoke(target, value);
            }
        } catch (Exception e) {
            // Property doesn't exist in this vehicle type, that's ok
        }
    }

    // Get customer's rented vehicles
    public List<PurchasedVehicle> getCustomerRentals(int customerId) {
        Optional<Customer> optionalCustomer = fileStorageService.findCustomerById(customerId);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();

            // First, try to get from the customer's rentedVehiclesList if available
            if (customer.getRentedVehiclesList() != null && !customer.getRentedVehiclesList().isEmpty()) {
                // Return all rentals, both active and history
                return customer.getRentedVehiclesList();
            }

            // Otherwise, get from the purchased vehicles file
            return fileStorageService.findPurchasedVehiclesByCustomerId(customer.getCustomerId());
        }

        return List.of();
    }

    // Get a specific rental by ID
    public PurchasedVehicle getRentalById(String purchaseId) {
        return fileStorageService.findPurchasedVehicleByPurchaseId(purchaseId).orElse(null);
    }

    // Return a vehicle that was previously rented
    public boolean returnVehicle(String purchaseId) {
        try {
            // Find the rental record
            Optional<PurchasedVehicle> rentalOpt = fileStorageService.findPurchasedVehicleByPurchaseId(purchaseId);
            if (rentalOpt.isPresent()) {
                PurchasedVehicle rental = rentalOpt.get();
                Vehicle vehicle = rental.getVehicle();

                if (vehicle == null) {
                    System.out.println("Error: Rental has no associated vehicle: " + purchaseId);
                    return false;
                }

                // Debug vehicle data
                System.out.println("Returning vehicle: " + vehicle.getVehicleID() +
                        ", Brand: " + vehicle.getBrand() +
                        ", Model: " + vehicle.getModel() +
                        ", Price: " + vehicle.getRentPrice() +
                        ", Image: " + vehicle.getImagePath());

                // Get the customer
                Customer customer = rental.getCustomer();
                if (customer == null) {
                    System.out.println("Error: Rental has no associated customer: " + purchaseId);
                    return false;
                }

                // Mark the rental as not rented
                rental.setRented(false);

                // Sync customer from storage to ensure we have the latest data
                Optional<Customer> latestCustomerOpt = fileStorageService.findCustomerById(customer.getCustomerId());
                if (latestCustomerOpt.isPresent()) {
                    customer = latestCustomerOpt.get();
                } else {
                    System.out.println("Warning: Could not find customer with ID: " + customer.getCustomerId());
                }

                // Instead of removing the rental from the customer's list, just update its
                // status to not rented
                boolean updated = false;
                if (customer.getRentedVehiclesList() != null) {
                    for (PurchasedVehicle pv : customer.getRentedVehiclesList()) {
                        // Update the rental status if we find it in the list
                        if (pv.getPurchaseId() != null && pv.getPurchaseId().equals(purchaseId)) {
                            pv.setRented(false);
                            updated = true;
                            System.out.println("Updated vehicle status in customer's rental history: " + purchaseId);
                            break;
                        }
                    }

                    if (!updated) {
                        System.out.println(
                                "Warning: Could not find vehicle in customer's rented list to update: " + purchaseId);

                        // Debug info about current rentals in the customer's list
                        System.out.println(
                                "Customer has " + customer.getRentedVehiclesList().size() + " rentals in history");
                        customer.getRentedVehiclesList()
                                .forEach(pv -> System.out.println("  - Purchase ID: " + pv.getPurchaseId() +
                                        ", Vehicle ID: "
                                        + (pv.getVehicle() != null ? pv.getVehicle().getVehicleID() : "null")));
                    }

                    // Save the updated customer to storage
                    Customer savedCustomer = fileStorageService.saveCustomer(customer);
                    System.out.println("Saved customer with " + savedCustomer.getRentedVehiclesList().size()
                            + " rentals in history");
                }

                // Update global list of purchased vehicles to mark this rental as returned
                List<PurchasedVehicle> allPurchasedVehicles = fileStorageService.getAllPurchasedVehicles();
                for (PurchasedVehicle pv : allPurchasedVehicles) {
                    if (pv.getPurchaseId() != null && pv.getPurchaseId().equals(purchaseId)) {
                        pv.setRented(false);
                        System.out.println("Marked global purchase as not rented: " + purchaseId);
                    }
                }
                fileStorageService.saveAllPurchasedVehicles(allPurchasedVehicles);

                // Find the vehicle in inventory and mark it as available
                Vehicle[] vehicles = vehicleInventory.getVehicles();
                boolean vehicleUpdatedInMemory = false;

                for (int i = 0; i < vehicleInventory.getVehicleCount(); i++) {
                    if (vehicles[i] != null && vehicles[i].getVehicleID().equals(vehicle.getVehicleID())) {
                        vehicles[i].setRented(false);
                        vehicleUpdatedInMemory = true;
                        System.out
                                .println("Vehicle marked as not rented in memory inventory: " + vehicle.getVehicleID());
                        break;
                    }
                }

                // If vehicle wasn't found in memory inventory, add it
                if (!vehicleUpdatedInMemory) {
                    // Ensure we have a complete vehicle with all details
                    Vehicle completeVehicle = createDeepCopy(vehicle);
                    completeVehicle.setRented(false);

                    // Add to in-memory inventory
                    vehicleInventory.addVehicle(completeVehicle);
                    System.out.println("Vehicle added to in-memory inventory: " + completeVehicle.getVehicleID());
                }

                // Update the vehicle in file storage as well
                Optional<Vehicle> storedVehicle = fileStorageService.findVehicleById(vehicle.getVehicleID());
                if (storedVehicle.isPresent()) {
                    // Update the existing vehicle's rental status
                    Vehicle existingVehicle = storedVehicle.get();
                    existingVehicle.setRented(false);
                    fileStorageService.saveVehicle(existingVehicle);
                    System.out.println("Vehicle marked as not rented in file storage: " + vehicle.getVehicleID());
                } else {
                    // If not in file storage, add it
                    Vehicle completeVehicle = createDeepCopy(vehicle);
                    completeVehicle.setRented(false);
                    fileStorageService.saveVehicle(completeVehicle);
                    System.out.println("Vehicle added to file storage: " + completeVehicle.getVehicleID());
                }

                // Update the rental record in storage
                fileStorageService.updatePurchasedVehicle(rental);

                return true;
            } else {
                System.out.println("Rental not found with ID: " + purchaseId);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error returning vehicle: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
