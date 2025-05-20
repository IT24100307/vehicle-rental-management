package com.system.project1.controller;

import com.system.project1.VehicleInventory;
import com.system.project1.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.system.project1.service.VehicleService;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin("*") // Allow cross-origin requests for easier development
public class VehicleController {

    @Autowired
    private VehicleInventory vehicleInventory;

    @Autowired
    private VehicleService vehicleService;

    private static final String IMAGE_DIR = "src/main/resources/static/images/";

    @PostMapping("/add")
    public ResponseEntity<?> addVehicle(@RequestBody Vehicle vehicle) {
        // First add to inventory
        vehicleInventory.addVehicle(vehicle);

        // Then save to file storage
        vehicleService.saveVehicle(vehicle);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Vehicle added successfully!");
        response.put("vehicleId", vehicle.getVehicleID());
        response.put("vehicleType", vehicleInventory.getVehicleTypeAsInt(vehicle));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{vehicleID}")
    public ResponseEntity<?> deleteVehicle(@PathVariable String vehicleID) {
        // First delete from memory inventory
        boolean deletedFromInventory = vehicleInventory.deleteVehicle(vehicleID);

        // Then delete from file storage
        boolean deletedFromStorage = vehicleService.deleteVehicle(vehicleID);

        if (deletedFromInventory || deletedFromStorage) {
            return ResponseEntity.ok("Vehicle deleted successfully!");
        } else {
            return ResponseEntity.badRequest().body("Vehicle not found!");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateVehicle(@RequestBody Vehicle vehicle) {
        // If the originalVehicleID wasn't set in the request but was included in a
        // custom property
        if (vehicle.getOriginalVehicleID() == null && vehicle.getVehicleID() != null) {
            // Use the current ID as the original
            vehicle.setOriginalVehicleID(vehicle.getVehicleID());
        }

        // Update in both storage mechanisms
        Vehicle updatedVehicle = vehicleService.updateVehicle(vehicle);

        if (updatedVehicle != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vehicle updated successfully!");
            response.put("vehicleType", vehicleInventory.getVehicleTypeAsInt(vehicle));
            response.put("vehicleId", vehicle.getVehicleID());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("Failed to update vehicle!");
        }
    }

    @GetMapping("/view")
    public ResponseEntity<?> viewVehicles() {
        // Get the full list of vehicles from both inventory and file storage
        List<Vehicle> allVehicles = vehicleService.getAllVehicles();

        // Filter out rented vehicles, only show available ones
        List<Vehicle> availableVehicles = new ArrayList<>();
        for (Vehicle vehicle : allVehicles) {
            if (vehicle != null && !vehicle.isRented()) {
                availableVehicles.add(vehicle);
            }
        }

        return ResponseEntity.ok(availableVehicles);
    }

    @GetMapping("/available")
    public ResponseEntity<?> availableVehicles() {
        // Return the same results as the /view endpoint - both return available
        // vehicles
        List<Vehicle> allVehicles = vehicleService.getAllVehicles();

        // Filter out rented vehicles, only show available ones
        List<Vehicle> availableVehicles = new ArrayList<>();
        for (Vehicle vehicle : allVehicles) {
            if (vehicle != null && !vehicle.isRented()) {
                availableVehicles.add(vehicle);
            }
        }

        return ResponseEntity.ok(availableVehicles);
    }

    @GetMapping("/filter/{type}")
    public ResponseEntity<?> filterVehiclesByType(@PathVariable int type) {
        // The filterVehiclesByType method properly filters and removes nulls
        Vehicle[] filteredVehicles = vehicleInventory.filterVehiclesByType(type);

        // Convert to List for better JSON serialization and filter out rented vehicles
        List<Vehicle> vehicleList = new ArrayList<>();
        for (Vehicle v : filteredVehicles) {
            if (v != null && !v.isRented()) {
                vehicleList.add(v);
            }
        }

        return ResponseEntity.ok(vehicleList);
    }

    @GetMapping("/type/{vehicleID}")
    public ResponseEntity<?> getVehicleType(@PathVariable String vehicleID) {
        for (int i = 0; i < vehicleInventory.getVehicleCount(); i++) {
            Vehicle[] vehicles = vehicleInventory.getVehicles();
            if (vehicles[i] != null && vehicles[i].getVehicleID().equals(vehicleID)) {
                int type = vehicleInventory.getVehicleTypeAsInt(vehicles[i]);
                Map<String, Object> response = new HashMap<>();
                response.put("vehicleType", type);
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.badRequest().body("Vehicle not found!");
    }

    @PostMapping("/uploadImage/{vehicleID}")
    public ResponseEntity<?> uploadImage(@PathVariable String vehicleID, @RequestParam("file") MultipartFile file) {
        try {
            // Create directory if it doesn't exist
            Path dirPath = Paths.get(IMAGE_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Generate a unique filename using vehicle ID and original filename
            String fileName = vehicleID + "_" + file.getOriginalFilename();
            Path path = Paths.get(IMAGE_DIR + fileName);

            // Save the file
            Files.write(path, file.getBytes());

            // Update vehicle with image path - using the correct URL path format
            boolean imageUpdated = false;
            for (int i = 0; i < vehicleInventory.getVehicleCount(); i++) {
                if (vehicleInventory.getVehicles()[i] != null &&
                        vehicleInventory.getVehicles()[i].getVehicleID().equals(vehicleID)) {
                    // Use the consistent path format that matches our resource handler
                    // configuration
                    vehicleInventory.getVehicles()[i].setImagePath("/images/" + fileName);
                    imageUpdated = true;

                    // Also update in file storage
                    Vehicle vehicle = vehicleInventory.getVehicles()[i];
                    vehicleService.updateVehicle(vehicle);

                    break;
                }
            }

            if (imageUpdated) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Image uploaded successfully!");
                response.put("imagePath", "/images/" + fileName);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("Vehicle not found!");
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to upload image: " + e.getMessage());
        }
    }
}