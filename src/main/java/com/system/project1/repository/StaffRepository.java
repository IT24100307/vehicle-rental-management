package com.system.project1.repository;

import com.system.project1.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    // We inherit basic CRUD operations from JpaRepository
    // Custom query methods can be added here if needed
}