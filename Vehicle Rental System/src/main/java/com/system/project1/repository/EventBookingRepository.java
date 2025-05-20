package com.system.project1.repository;

import com.system.project1.entity.EventBooking;
import com.system.project1.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EventBookingRepository extends JpaRepository<EventBooking, Long> {
    List<EventBooking> findByCustomer(Customer customer);

    List<EventBooking> findByEventId(Long eventId);

    List<EventBooking> findByEventDateBetween(Date startDate, Date endDate);

    List<EventBooking> findByStatus(String status);

    EventBooking findByBookingId(String bookingId);
}
