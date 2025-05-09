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

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin("*") // Allow cross-origin requests for easier development
public class VehicleController {

    @Autowired
    private VehicleInventory vehicleInventory;

    private static final String IMAGE_DIR = "src/main/resources/static/images/";

    @PostMapping("/add")
    public ResponseEntity<?> addVehicle(@RequestBody Vehicle vehicle) {
        vehicleInventory.addVehicle(vehicle);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Vehicle added successfully!");
        response.put("vehicleId", vehicle.getVehicleID());
        response.put("vehicleType", vehicleInventory.getVehicleTypeAsInt(vehicle));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{vehicleID}")
    public ResponseEntity<?> deleteVehicle(@PathVariable String vehicleID) {
        boolean deleted = vehicleInventory.deleteVehicle(vehicleID);
        if (deleted) {
            return ResponseEntity.ok("Vehicle deleted successfully!");
        } else {
            return ResponseEntity.badRequest().body("Vehicle not found!");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateVehicle(@RequestBody Vehicle vehicle) {
        boolean updated = vehicleInventory.updateVehicle(vehicle.getVehicleID(), vehicle);
        if (updated) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vehicle updated successfully!");
            response.put("vehicleType", vehicleInventory.getVehicleTypeAsInt(vehicle));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("Vehicle not found!");
        }
    }

    @GetMapping("/view")
    public ResponseEntity<?> viewVehicles() {
        // Get the full list of vehicles from inventory
        Vehicle[] allVehicles = vehicleInventory.getVehicles();
        // Create a list to store non-null vehicles
        List<Vehicle> validVehicles = new ArrayList<>();

        // Add only non-null vehicles to the list
        for (int i = 0; i < vehicleInventory.getVehicleCount(); i++) {
            if (allVehicles[i] != null) {
                validVehicles.add(allVehicles[i]);
            }
        }

        return ResponseEntity.ok(validVehicles);
    }

    @GetMapping("/filter/{type}")
    public ResponseEntity<?> filterVehiclesByType(@PathVariable int type) {
        // The filterVehiclesByType method properly filters and removes nulls
        Vehicle[] filteredVehicles = vehicleInventory.filterVehiclesByType(type);

        // Convert to List for better JSON serialization
        List<Vehicle> vehicleList = new ArrayList<>();
        for (Vehicle v : filteredVehicles) {
            if (v != null) {
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