package com.system.project1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    // The JpaRepository provides all the standard CRUD operations
    // No need to define basic methods like findAll(), findById(), save(),
    // deleteById()
}
