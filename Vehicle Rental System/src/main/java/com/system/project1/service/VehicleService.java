package com.system.project1.service;

import com.system.project1.VehicleInventory;
import com.system.project1.entity.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private VehicleInventory vehicleInventory;

    public Vehicle saveVehicle(Vehicle vehicle) {
        return fileStorageService.saveVehicle(vehicle);
    }

    public Vehicle findVehicleById(String id) {
        // First try to find from file storage
        Optional<Vehicle> fileVehicle = fileStorageService.findVehicleById(id);
        if (fileVehicle.isPresent()) {
            return fileVehicle.get();
        }

        // If not found in file storage, check VehicleInventory
        Vehicle[] vehicles = vehicleInventory.getVehicles();
        int count = vehicleInventory.getVehicleCount();

        for (int i = 0; i < count; i++) {
            if (vehicles[i] != null && vehicles[i].getVehicleID().equals(id)) {
                return vehicles[i];
            }
        }

        return null;
    }

    public boolean vehicleExistsInDatabase(String id) {
        return fileStorageService.findVehicleById(id).isPresent();
    }

    public List<Vehicle> getAllVehicles() {
        List<Vehicle> allVehicles = new ArrayList<>(fileStorageService.getAllVehicles());

        // Also add vehicles from the inventory
        Vehicle[] inventoryVehicles = vehicleInventory.getVehicles();
        int count = vehicleInventory.getVehicleCount();

        for (int i = 0; i < count; i++) {
            if (inventoryVehicles[i] != null) {
                // Check if the vehicle is not already in the list (to avoid duplicates)
                boolean alreadyExists = false;
                for (Vehicle existingVehicle : allVehicles) {
                    if (existingVehicle.getVehicleID().equals(inventoryVehicles[i].getVehicleID())) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    allVehicles.add(inventoryVehicles[i]);
                }
            }
        }

        return allVehicles;
    }

    public boolean deleteVehicle(String id) {
        // Update both storage mechanisms
        boolean removedFromStorage = fileStorageService.deleteVehicle(id);
        boolean removedFromInventory = vehicleInventory.deleteVehicle(id);

        // Return true if it was removed from either storage mechanism
        return removedFromStorage || removedFromInventory;
    }

    public Vehicle updateVehicle(Vehicle vehicle) {
        try {
            // Check if it's an ID change by storing the original vehicle ID
            String originalVehicleID = vehicle.getOriginalVehicleID();
            String currentVehicleID = vehicle.getVehicleID();

            // If originalVehicleID is not set, use the current ID
            if (originalVehicleID == null || originalVehicleID.isEmpty()) {
                originalVehicleID = currentVehicleID;
            }

            // Check if the ID has changed
            boolean idChanged = !originalVehicleID.equals(currentVehicleID);

            // Find the existing vehicle
            Optional<Vehicle> existingVehicle = fileStorageService.findVehicleById(originalVehicleID);

            if (idChanged && existingVehicle.isPresent()) {
                // ID is changing, delete the old record first
                fileStorageService.deleteVehicle(originalVehicleID);

                // Also delete from inventory if it exists there
                vehicleInventory.deleteVehicle(originalVehicleID);
            }

            // Update in memory inventory (will add it if it doesn't exist)
            vehicleInventory.updateVehicle(originalVehicleID, vehicle);

            // Then update/create in file storage
            Vehicle updatedVehicle = fileStorageService.saveVehicle(vehicle);

            return updatedVehicle;
        } catch (Exception e) {
            System.err.println("Error updating vehicle: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all vehicles available for booking (not currently assigned to any event)
     * 
     * @return List of available vehicles
     */
    public List<Vehicle> getAvailableVehicles() {
        // Get vehicles from both file storage and inventory
        List<Vehicle> allVehicles = getAllVehicles();

        // Filter out rented vehicles
        List<Vehicle> availableVehicles = new ArrayList<>();
        for (Vehicle vehicle : allVehicles) {
            if (vehicle != null && !vehicle.isRented()) {
                availableVehicles.add(vehicle);
            }
        }

        // Log the number of vehicles found to help with debugging
        System.out.println("Found " + availableVehicles.size()
                + " available vehicles for events (after filtering rented vehicles)");

        return availableVehicles;
    }
}