package com.system.project1.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.system.project1.PurchaseLinkedList;
import com.system.project1.VehicleInventory;
import com.system.project1.entity.PurchasedVehicle;
import com.system.project1.entity.Vehicle;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    @Autowired
    private PurchaseLinkedList purchaseLinkedList;

    @Autowired
    private VehicleInventory vehicleInventory;

    // Create a new purchase
    @PostMapping
    public ResponseEntity<?> createPurchase(@RequestBody Map<String, Object> purchaseData) {
        try {
            String vehicleId = (String) purchaseData.get("vehicleId");
            String customerName = (String) purchaseData.get("customerName");
            String contactNumber = (String) purchaseData.get("contactNumber");

            // Find the vehicle in inventory
            Vehicle[] allVehicles = vehicleInventory.getVehicles();
            Vehicle selectedVehicle = null;

            for (int i = 0; i < vehicleInventory.getVehicleCount(); i++) {
                if (allVehicles[i] != null && allVehicles[i].getVehicleID().equals(vehicleId)) {
                    selectedVehicle = allVehicles[i];
                    break;
                }
            }

            if (selectedVehicle == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Vehicle not found with ID: " + vehicleId));
            }

            // Create a unique purchase ID
            String purchaseId = "PUR-" + UUID.randomUUID().toString().substring(0, 8);

            // Create the purchased vehicle object
            PurchasedVehicle purchasedVehicle = new PurchasedVehicle(
                    purchaseId, selectedVehicle, customerName, contactNumber);

            // Add to the linked list
            purchaseLinkedList.addPurchase(purchasedVehicle);

            // Remove from inventory if successfully added to purchases
            vehicleInventory.deleteVehicle(vehicleId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Vehicle purchased successfully",
                            "purchaseId", purchaseId));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process purchase: " + e.getMessage()));
        }
    }

    // Get all purchases
    @GetMapping
    public ResponseEntity<?> getAllPurchases() {
        try {
            PurchasedVehicle[] purchases = purchaseLinkedList.getAllPurchases();
            return ResponseEntity.ok(purchases);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve purchases: " + e.getMessage()));
        }
    }

    // Get a specific purchase by ID
    @GetMapping("/{purchaseId}")
    public ResponseEntity<?> getPurchaseById(@PathVariable String purchaseId) {
        try {
            PurchasedVehicle purchase = purchaseLinkedList.getPurchaseById(purchaseId);
            if (purchase == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Purchase not found with ID: " + purchaseId));
            }
            return ResponseEntity.ok(purchase);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve purchase: " + e.getMessage()));
        }
    }

    // Update a purchase
    @PutMapping("/{purchaseId}")
    public ResponseEntity<?> updatePurchase(@PathVariable String purchaseId,
            @RequestBody PurchasedVehicle updatedPurchase) {
        try {
            boolean updated = purchaseLinkedList.updatePurchase(purchaseId, updatedPurchase);
            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Purchase not found with ID: " + purchaseId));
            }
            return ResponseEntity.ok(Map.of("message", "Purchase updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update purchase: " + e.getMessage()));
        }
    }

    // Update rental status
    @PatchMapping("/{purchaseId}/rental-status")
    public ResponseEntity<?> updateRentalStatus(@PathVariable String purchaseId,
            @RequestBody Map<String, Boolean> statusUpdate) {
        try {
            Boolean isRented = statusUpdate.get("isRented");
            if (isRented == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Missing 'isRented' field in request body"));
            }

            boolean updated = purchaseLinkedList.updateRentalStatus(purchaseId, isRented);
            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Purchase not found with ID: " + purchaseId));
            }

            String status = isRented ? "rented" : "available";
            return ResponseEntity.ok(Map.of("message", "Vehicle marked as " + status + " successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update rental status: " + e.getMessage()));
        }
    }

    // Delete a purchase
    @DeleteMapping("/{purchaseId}")
    public ResponseEntity<?> deletePurchase(@PathVariable String purchaseId) {
        try {
            // Get the purchase first to potentially return the vehicle to inventory
            PurchasedVehicle purchase = purchaseLinkedList.getPurchaseById(purchaseId);
            if (purchase == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Purchase not found with ID: " + purchaseId));
            }

            // Store the vehicle before deleting the purchase
            Vehicle vehicle = purchase.getVehicle();

            // Delete the purchase
            boolean deleted = purchaseLinkedList.deletePurchase(purchaseId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to delete purchase"));
            }

            // Add the vehicle back to inventory
            if (vehicle != null) {
                vehicleInventory.addVehicle(vehicle);
            }

            return ResponseEntity
                    .ok(Map.of("message", "Purchase deleted successfully and vehicle returned to inventory"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete purchase: " + e.getMessage()));
        }
    }

    // Get rented vehicles
    @GetMapping("/rented")
    public ResponseEntity<?> getRentedVehicles() {
        try {
            PurchasedVehicle[] rentedVehicles = purchaseLinkedList.getRentedVehicles();
            return ResponseEntity.ok(rentedVehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve rented vehicles: " + e.getMessage()));
        }
    }

    // Get available vehicles
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableVehicles() {
        try {
            PurchasedVehicle[] availableVehicles = purchaseLinkedList.getAvailableVehicles();
            return ResponseEntity.ok(availableVehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve available vehicles: " + e.getMessage()));
        }
    }
}