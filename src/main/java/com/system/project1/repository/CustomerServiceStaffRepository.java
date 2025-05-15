package com.system.project1.repository;

import com.system.project1.entity.CustomerServiceStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerServiceStaffRepository extends JpaRepository<CustomerServiceStaff, Long> {
}