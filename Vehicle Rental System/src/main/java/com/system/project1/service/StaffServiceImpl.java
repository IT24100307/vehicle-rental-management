package com.system.project1.service;

import com.system.project1.entity.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StaffServiceImpl implements StaffService {

    private final FileStorageService fileStorageService;

    @Autowired
    public StaffServiceImpl(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public Staff saveStaff(Staff staff) {
        return fileStorageService.saveStaff(staff);
    }

    @Override
    public List<Staff> getAllStaff() {
        return fileStorageService.getAllStaff();
    }

    @Override
    public Staff getStaffById(Long id) {
        Optional<Staff> optionalStaff = fileStorageService.findStaffById(id);
        return optionalStaff.orElse(null);
    }

    @Override
    public Staff updateStaff(Staff staff) {
        // Check if staff exists
        if (fileStorageService.findStaffById(staff.getId()).isPresent()) {
            return fileStorageService.saveStaff(staff);
        }
        return null;
    }

    @Override
    public void deleteStaff(Long id) {
        // Use the dedicated delete method in FileStorageService
        boolean deleted = fileStorageService.deleteStaff(id);

        if (deleted) {
            System.out.println("Staff with ID " + id + " was successfully deleted");
        } else {
            System.out.println("Staff with ID " + id + " not found");
        }
    }
}