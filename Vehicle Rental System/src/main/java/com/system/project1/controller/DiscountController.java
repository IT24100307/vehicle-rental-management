package com.system.project1.controller;

import com.system.project1.entity.Discount;
import com.system.project1.entity.Staff;
import com.system.project1.service.DiscountService;
import com.system.project1.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private StaffService staffService;

    // Initialize default discounts
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initializeDiscounts() {
        discountService.initializeDefaultDiscounts();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Default discounts initialized successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Get all discounts
    @GetMapping
    public ResponseEntity<List<Discount>> getAllDiscounts() {
        List<Discount> discounts = discountService.getAllDiscounts();
        return new ResponseEntity<>(discounts, HttpStatus.OK);
    }

    // Get discount by ID
    @GetMapping("/{id}")
    public ResponseEntity<Discount> getDiscountById(@PathVariable Long id) {
        Discount discount = discountService.getDiscountById(id);
        if (discount != null) {
            return new ResponseEntity<>(discount, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Get discounts by staff ID
    @GetMapping("/by-staff/{staffId}")
    public ResponseEntity<List<Discount>> getDiscountsByStaffId(@PathVariable Long staffId) {
        List<Discount> discounts = discountService.getAllDiscounts()
                .stream()
                .filter(d -> staffId.equals(d.getStaffId()))
                .collect(Collectors.toList());
        return new ResponseEntity<>(discounts, HttpStatus.OK);
    }

    // Get all staff
    @GetMapping("/staff")
    public ResponseEntity<List<Staff>> getAllStaff() {
        List<Staff> staffList = staffService.getAllStaff();
        return new ResponseEntity<>(staffList, HttpStatus.OK);
    }

    // Create a new discount
    @PostMapping
    public ResponseEntity<Discount> createDiscount(@RequestBody Discount discount) {
        Discount createdDiscount = discountService.createDiscount(discount);
        return new ResponseEntity<>(createdDiscount, HttpStatus.CREATED);
    }

    // Update a discount
    @PutMapping("/{id}")
    public ResponseEntity<Discount> updateDiscount(@PathVariable Long id, @RequestBody Discount discountDetails) {
        Discount updatedDiscount = discountService.updateDiscount(id, discountDetails);
        if (updatedDiscount != null) {
            return new ResponseEntity<>(updatedDiscount, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete a discount
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteDiscount(@PathVariable Long id) {
        boolean deleted = discountService.deleteDiscount(id);

        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", deleted);

        if (deleted) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Toggle discount active status
    @PutMapping("/{id}/toggle")
    public ResponseEntity<Discount> toggleDiscountStatus(@PathVariable Long id) {
        Discount discount = discountService.getDiscountById(id);
        if (discount != null) {
            discount.setActive(!discount.isActive());
            Discount updatedDiscount = discountService.updateDiscount(id, discount);
            return new ResponseEntity<>(updatedDiscount, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Calculate discount for a test amount
    @GetMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateDiscountExample(
            @RequestParam double amount,
            @RequestParam double discountPercentage) {

        double discountedAmount = discountService.applyDiscount(amount, discountPercentage);

        Map<String, Object> response = new HashMap<>();
        response.put("originalAmount", amount);
        response.put("discountPercentage", discountPercentage);
        response.put("discountedAmount", discountedAmount);
        response.put("savings", amount - discountedAmount);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}