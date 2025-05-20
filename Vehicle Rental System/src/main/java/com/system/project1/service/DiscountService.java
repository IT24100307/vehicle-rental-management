package com.system.project1.service;

import com.system.project1.entity.Customer;
import com.system.project1.entity.Discount;
import com.system.project1.entity.PurchasedVehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    @Autowired
    private FileStorageService fileStorageService;

    // Create a new discount
    public Discount createDiscount(Discount discount) {
        return fileStorageService.saveDiscount(discount);
    }

    // Get all discounts
    public List<Discount> getAllDiscounts() {
        return fileStorageService.getAllDiscounts();
    }

    // Get discount by ID
    public Discount getDiscountById(Long id) {
        return fileStorageService.findDiscountById(id).orElse(null);
    }

    // Update a discount
    public Discount updateDiscount(Long id, Discount discountDetails) {
        Optional<Discount> discountOpt = fileStorageService.findDiscountById(id);
        if (discountOpt.isPresent()) {
            Discount discount = discountOpt.get();
            discount.setName(discountDetails.getName());
            discount.setDescription(discountDetails.getDescription());
            discount.setDiscountPercentage(discountDetails.getDiscountPercentage());
            discount.setActive(discountDetails.isActive());
            discount.setDiscountType(discountDetails.getDiscountType());
            discount.setStartDate(discountDetails.getStartDate());
            discount.setEndDate(discountDetails.getEndDate());
            discount.setMinimumRides(discountDetails.getMinimumRides());
            discount.setApplyToAllVehicles(discountDetails.isApplyToAllVehicles());

            return fileStorageService.saveDiscount(discount);
        }
        return null;
    }

    // Delete a discount
    public boolean deleteDiscount(Long id) {
        try {
            // Use the dedicated delete method from FileStorageService
            boolean deleted = fileStorageService.deleteDiscount(id);

            if (deleted) {
                System.out.println("Discount with ID " + id + " was successfully deleted");
            } else {
                System.out.println("Discount with ID " + id + " not found");
            }

            return deleted;
        } catch (Exception e) {
            System.err.println("Error deleting discount with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Create default seasonal discounts if they don't exist
    public void initializeDefaultDiscounts() {
        // Check if seasonal discounts exist
        List<Discount> seasonalDiscounts = findByDiscountType(Discount.DiscountType.SEASONAL);
        if (seasonalDiscounts.isEmpty()) {
            // Create Christmas discount
            Discount christmasDiscount = new Discount(
                    "Christmas Special",
                    "20% off on all rentals during Christmas period",
                    20.0,
                    Discount.DiscountType.SEASONAL);
            christmasDiscount.setStartDate(LocalDate.of(LocalDate.now().getYear(), Month.DECEMBER, 20));
            christmasDiscount.setEndDate(LocalDate.of(LocalDate.now().getYear(), Month.DECEMBER, 31));
            fileStorageService.saveDiscount(christmasDiscount);

            // Create New Year discount
            Discount newYearDiscount = new Discount(
                    "New Year Special",
                    "20% off on all rentals during New Year period",
                    20.0,
                    Discount.DiscountType.SEASONAL);
            newYearDiscount.setStartDate(LocalDate.of(LocalDate.now().getYear(), Month.JANUARY, 1));
            newYearDiscount.setEndDate(LocalDate.of(LocalDate.now().getYear(), Month.JANUARY, 7));
            fileStorageService.saveDiscount(newYearDiscount);

            // Create loyalty discount
            Discount loyaltyDiscount = new Discount(
                    "Loyalty Discount",
                    "10% off for customers with at least 2 previous rides",
                    10.0,
                    Discount.DiscountType.LOYALTY);
            loyaltyDiscount.setMinimumRides(2);
            fileStorageService.saveDiscount(loyaltyDiscount);
        }
    }

    // Helper method to find discounts by type
    private List<Discount> findByDiscountType(Discount.DiscountType discountType) {
        return fileStorageService.getAllDiscounts().stream()
                .filter(d -> d.getDiscountType() == discountType)
                .collect(Collectors.toList());
    }

    // Helper method to find active seasonal discounts for a date
    private List<Discount> findActiveSeasonalDiscounts(LocalDate currentDate) {
        return fileStorageService.getAllDiscounts().stream()
                .filter(d -> d.getDiscountType() == Discount.DiscountType.SEASONAL
                        && d.isActive()
                        && d.getStartDate() != null
                        && d.getEndDate() != null
                        && !currentDate.isBefore(d.getStartDate())
                        && !currentDate.isAfter(d.getEndDate()))
                .collect(Collectors.toList());
    }

    // Helper method to find active loyalty discounts
    private List<Discount> findByIsActiveTrueAndDiscountType(Discount.DiscountType discountType) {
        return fileStorageService.getAllDiscounts().stream()
                .filter(d -> d.isActive() && d.getDiscountType() == discountType)
                .collect(Collectors.toList());
    }

    // Helper method to find global discounts
    private List<Discount> findByIsActiveTrueAndApplyToAllVehiclesTrue() {
        return fileStorageService.getAllDiscounts().stream()
                .filter(d -> d.isActive() && d.isApplyToAllVehicles())
                .collect(Collectors.toList());
    }

    // Calculate the applicable discount for a customer and date
    public double calculateApplicableDiscount(Customer customer, LocalDate rentalDate) {
        double maxDiscountPercentage = 0.0;

        // Check for seasonal discounts
        List<Discount> seasonalDiscounts = findActiveSeasonalDiscounts(rentalDate);
        for (Discount discount : seasonalDiscounts) {
            if (discount.getDiscountPercentage() > maxDiscountPercentage) {
                maxDiscountPercentage = discount.getDiscountPercentage();
            }
        }

        // Check for loyalty discounts
        if (customer != null) {
            List<Discount> loyaltyDiscounts = findByIsActiveTrueAndDiscountType(Discount.DiscountType.LOYALTY);
            for (Discount discount : loyaltyDiscounts) {
                if (discount.getMinimumRides() != null &&
                        customer.getRentedVehiclesList() != null &&
                        customer.getRentedVehiclesList().size() >= discount.getMinimumRides() &&
                        discount.getDiscountPercentage() > maxDiscountPercentage) {
                    maxDiscountPercentage = discount.getDiscountPercentage();
                }
            }
        }

        // Check for global discounts
        List<Discount> globalDiscounts = findByIsActiveTrueAndApplyToAllVehiclesTrue();
        for (Discount discount : globalDiscounts) {
            if (discount.getDiscountPercentage() > maxDiscountPercentage) {
                maxDiscountPercentage = discount.getDiscountPercentage();
            }
        }

        return maxDiscountPercentage;
    }

    // Apply discount to rental amount
    public double applyDiscount(double originalAmount, double discountPercentage) {
        if (discountPercentage <= 0) {
            return originalAmount;
        }

        double discountAmount = originalAmount * (discountPercentage / 100.0);
        return originalAmount - discountAmount;
    }
}