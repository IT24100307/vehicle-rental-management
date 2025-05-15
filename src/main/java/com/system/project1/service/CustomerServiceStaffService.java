package com.system.project1.service;

import com.system.project1.entity.CustomerServiceStaff;
import com.system.project1.repository.CustomerServiceStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceStaffService {
    private final CustomerServiceStaffRepository repository;

    @Autowired
    public CustomerServiceStaffService(CustomerServiceStaffRepository repository) {
        this.repository = repository;
    }

    public CustomerServiceStaff createStaff(CustomerServiceStaff staff) {
        return repository.save(staff);
    }

    public List<CustomerServiceStaff> getAllStaff() {
        return repository.findAll();
    }

    public Optional<CustomerServiceStaff> getStaffById(Long id) {
        return repository.findById(id);
    }

    public CustomerServiceStaff updateStaff(Long id, CustomerServiceStaff updatedStaff) {
        Optional<CustomerServiceStaff> existingStaff = repository.findById(id);
        if (existingStaff.isPresent()) {
            CustomerServiceStaff staff = existingStaff.get();
            staff.setFirstName(updatedStaff.getFirstName());
            staff.setLastName(updatedStaff.getLastName());
            staff.setEmail(updatedStaff.getEmail());
            staff.setPhoneNumber(updatedStaff.getPhoneNumber());
            staff.setDepartment(updatedStaff.getDepartment());
            return repository.save(staff);
        }
        return null;
    }

    public void deleteStaff(Long id) {
        repository.deleteById(id);
    }
}