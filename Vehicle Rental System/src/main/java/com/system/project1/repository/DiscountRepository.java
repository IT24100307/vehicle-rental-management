package com.system.project1.repository;

import com.system.project1.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    // Find all active discounts
    List<Discount> findByIsActiveTrue();

    // Find discounts by type
    List<Discount> findByDiscountType(Discount.DiscountType discountType);

    // Find active seasonal discounts for current date
    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND d.discountType = 'SEASONAL' AND d.startDate <= ?1 AND d.endDate >= ?1")
    List<Discount> findActiveSeasonalDiscounts(LocalDate currentDate);

    // Find active loyalty discounts
    List<Discount> findByIsActiveTrueAndDiscountType(Discount.DiscountType discountType);

    // Find global discounts applicable to all vehicles
    List<Discount> findByIsActiveTrueAndApplyToAllVehiclesTrue();
}