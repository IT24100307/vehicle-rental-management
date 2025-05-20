package com.system.project1.repository;

import com.system.project1.entity.Customer;
import com.system.project1.entity.PurchasedVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchasedVehicleRepository extends JpaRepository<PurchasedVehicle, Long> {
    List<PurchasedVehicle> findByCustomer(Customer customer);
}
