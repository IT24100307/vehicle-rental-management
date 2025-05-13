package com.system.project1;

import com.system.project1.entity.PurchasedVehicle;
import com.system.project1.entity.Vehicle;
import org.springframework.stereotype.Service;

@Service
public class PurchaseLinkedList {
    private PurchasedVehicle head;
    private int size;

    public PurchaseLinkedList() {
        this.head = null;
        this.size = 0;
    }

    // Add a new purchased vehicle to the linked list (Create)
    public void addPurchase(PurchasedVehicle purchasedVehicle) {
        if (purchasedVehicle == null) {
            return;
        }

        // Ensure the new node doesn't have any next references
        purchasedVehicle.setNext(null);

        // If the list is empty, make the new purchase the head
        if (head == null) {
            head = purchasedVehicle;
        } else {
            // Otherwise, traverse to the end and add it there
            PurchasedVehicle current = head;
            while (current.getNext() != null) {
                current = current.getNext();
            }
            current.setNext(purchasedVehicle);
        }
        size++;

        // Sort the list after adding a new purchase
        sortByRentalStatus();
    }

    // Get a purchased vehicle by its purchase ID (Read)
    public PurchasedVehicle getPurchaseById(String purchaseId) {
        if (head == null || purchaseId == null) {
            return null;
        }

        PurchasedVehicle current = head;
        while (current != null) {
            if (current.getPurchaseId().equals(purchaseId)) {
                return current;
            }
            current = current.getNext();
        }
        return null;
    }

    // Get purchases by vehicle ID
    public PurchasedVehicle[] getPurchasesByVehicleId(String vehicleId) {
        if (head == null || vehicleId == null) {
            return new PurchasedVehicle[0];
        }

        // First, count matches to create array of the right size
        int count = 0;
        PurchasedVehicle current = head;
        while (current != null) {
            if (current.getVehicle() != null && current.getVehicle().getVehicleID().equals(vehicleId)) {
                count++;
            }
            current = current.getNext();
        }

        // Create array and populate it
        PurchasedVehicle[] result = new PurchasedVehicle[count];
        if (count > 0) {
            current = head;
            int index = 0;
            while (current != null) {
                if (current.getVehicle() != null && current.getVehicle().getVehicleID().equals(vehicleId)) {
                    result[index++] = current;
                }
                current = current.getNext();
            }
        }
        return result;
    }

    // Update a purchased vehicle's information (Update)
    public boolean updatePurchase(String purchaseId, PurchasedVehicle updatedPurchase) {
        if (head == null || purchaseId == null || updatedPurchase == null) {
            return false;
        }

        PurchasedVehicle current = head;
        while (current != null) {
            if (current.getPurchaseId().equals(purchaseId)) {
                // Update the fields, but keep the next reference intact
                current.setCustomerName(updatedPurchase.getCustomerName());
                current.setContactNumber(updatedPurchase.getContactNumber());
                current.setRented(updatedPurchase.isRented());

                // Only update vehicle if it's provided
                if (updatedPurchase.getVehicle() != null) {
                    current.setVehicle(updatedPurchase.getVehicle());
                }

                // Only update purchase date if it's provided
                if (updatedPurchase.getPurchaseDate() != null) {
                    current.setPurchaseDate(updatedPurchase.getPurchaseDate());
                }

                // Sort after updating rental status
                sortByRentalStatus();
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    // Update rental status for a specific purchase
    public boolean updateRentalStatus(String purchaseId, boolean isRented) {
        if (head == null || purchaseId == null) {
            return false;
        }

        PurchasedVehicle current = head;
        while (current != null) {
            if (current.getPurchaseId().equals(purchaseId)) {
                current.setRented(isRented);
                // Sort after updating rental status
                sortByRentalStatus();
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    // Delete a purchased vehicle from the linked list (Delete)
    public boolean deletePurchase(String purchaseId) {
        if (head == null || purchaseId == null) {
            return false;
        }

        // If the head is the one to delete
        if (head.getPurchaseId().equals(purchaseId)) {
            head = head.getNext();
            size--;
            return true;
        }

        // Otherwise, find the node before the one to delete
        PurchasedVehicle current = head;
        while (current.getNext() != null) {
            if (current.getNext().getPurchaseId().equals(purchaseId)) {
                current.setNext(current.getNext().getNext());
                size--;
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    // Selection sort to sort the linked list by rental status
    // Available (not rented) vehicles come first
    public void sortByRentalStatus() {
        if (head == null || head.getNext() == null) {
            return; // Nothing to sort
        }

        PurchasedVehicle current = head;

        // Selection sort algorithm adapted for linked list
        while (current != null) {
            // Find the node with the minimum value (not rented comes first)
            PurchasedVehicle minNode = current;
            PurchasedVehicle temp = current.getNext();

            while (temp != null) {
                // If temp is not rented and minNode is rented, or both have same rental status
                if ((!temp.isRented() && minNode.isRented()) ||
                        (temp.isRented() == minNode.isRented())) {
                    minNode = temp;
                }
                temp = temp.getNext();
            }

            // Swap data if minNode is not current
            if (minNode != current) {
                // Swap just the data, not the nodes themselves
                String tempPurchaseId = current.getPurchaseId();
                Vehicle tempVehicle = current.getVehicle();
                String tempCustomerName = current.getCustomerName();
                String tempContactNumber = current.getContactNumber();
                java.util.Date tempPurchaseDate = current.getPurchaseDate();
                boolean tempIsRented = current.isRented();

                current.setPurchaseId(minNode.getPurchaseId());
                current.setVehicle(minNode.getVehicle());
                current.setCustomerName(minNode.getCustomerName());
                current.setContactNumber(minNode.getContactNumber());
                current.setPurchaseDate(minNode.getPurchaseDate());
                current.setRented(minNode.isRented());

                minNode.setPurchaseId(tempPurchaseId);
                minNode.setVehicle(tempVehicle);
                minNode.setCustomerName(tempCustomerName);
                minNode.setContactNumber(tempContactNumber);
                minNode.setPurchaseDate(tempPurchaseDate);
                minNode.setRented(tempIsRented);
            }

            current = current.getNext();
        }
    }

    // Get all purchases as an array
    public PurchasedVehicle[] getAllPurchases() {
        if (head == null) {
            return new PurchasedVehicle[0];
        }

        PurchasedVehicle[] purchasesArray = new PurchasedVehicle[size];
        PurchasedVehicle current = head;
        int index = 0;

        while (current != null) {
            purchasesArray[index++] = current;
            current = current.getNext();
        }

        return purchasesArray;
    }

    // Get only rented vehicles
    public PurchasedVehicle[] getRentedVehicles() {
        if (head == null) {
            return new PurchasedVehicle[0];
        }

        // Count rented vehicles first
        int count = 0;
        PurchasedVehicle current = head;
        while (current != null) {
            if (current.isRented()) {
                count++;
            }
            current = current.getNext();
        }

        // Create and populate the array
        PurchasedVehicle[] rentedVehicles = new PurchasedVehicle[count];
        if (count > 0) {
            current = head;
            int index = 0;
            while (current != null) {
                if (current.isRented()) {
                    rentedVehicles[index++] = current;
                }
                current = current.getNext();
            }
        }

        return rentedVehicles;
    }

    // Get available (not rented) vehicles
    public PurchasedVehicle[] getAvailableVehicles() {
        if (head == null) {
            return new PurchasedVehicle[0];
        }

        // Count available vehicles first
        int count = 0;
        PurchasedVehicle current = head;
        while (current != null) {
            if (!current.isRented()) {
                count++;
            }
            current = current.getNext();
        }

        // Create and populate the array
        PurchasedVehicle[] availableVehicles = new PurchasedVehicle[count];
        if (count > 0) {
            current = head;
            int index = 0;
            while (current != null) {
                if (!current.isRented()) {
                    availableVehicles[index++] = current;
                }
                current = current.getNext();
            }
        }

        return availableVehicles;
    }

    // Get the size of the linked list
    public int getSize() {
        return size;
    }

    // Check if the linked list is empty
    public boolean isEmpty() {
        return head == null;
    }
}