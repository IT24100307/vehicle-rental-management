package com.system.project1.controller;

import com.system.project1.entity.CustomerServiceStaff;
import com.system.project1.repository.CustomerServiceStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "http://localhost:8080") // Ensure CORS is enabled
public class CustomerServiceStaffController {

    @Autowired
    private CustomerServiceStaffRepository repository;

    private static final String FILE_PATH = "src/main/resources/staff_details.txt";

    // GET all staff members
    @GetMapping
    public List<CustomerServiceStaff> getAllStaff() {
        return repository.findAll();
    }

    // GET a specific staff member by ID
    @GetMapping("/{id}")
    public ResponseEntity<CustomerServiceStaff> getStaffById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST to add a new staff member
    @PostMapping
    public ResponseEntity<?> addStaff(@RequestBody CustomerServiceStaff staff) {
        try {
            CustomerServiceStaff savedStaff = repository.save(staff);
            updateTextFile();
            return ResponseEntity.ok(savedStaff);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error adding staff member: " + e.getMessage());
        }
    }

    // PUT to update an existing staff member
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable Long id, @RequestBody CustomerServiceStaff staff) {
        try {
            if (!repository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            staff.setId(id);
            CustomerServiceStaff updatedStaff = repository.save(staff);
            updateTextFile();
            return ResponseEntity.ok(updatedStaff);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating staff member: " + e.getMessage());
        }
    }

    // DELETE a staff member by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        try {
            if (repository.existsById(id)) {
                repository.deleteById(id);
                updateTextFile();
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting staff member: " + e.getMessage());
        }
    }

    private void updateTextFile() throws IOException {
        List<CustomerServiceStaff> staffList = repository.findAll();
        Path path = Paths.get(FILE_PATH);
        Files.createDirectories(path.getParent()); // Ensure directory exists
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
            writer.println("Customer Service Staff Details");
            writer.println("==============================");
            for (CustomerServiceStaff staff : staffList) {
                writer.println("ID: " + staff.getId());
                writer.println("First Name: " + staff.getFirstName());
                writer.println("Last Name: " + staff.getLastName());
                writer.println("Email: " + staff.getEmail());
                writer.println("Phone Number: " + staff.getPhoneNumber());
                writer.println("Department: " + staff.getDepartment());
                writer.println("------------------------------");
            }
        }
    }
}