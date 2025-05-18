package com.system.project1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.vehicleTypes WHERE e.eventType = :eventType")
    List<Event> findByEventType(String eventType);

    @Override
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.vehicleTypes")
    @NonNull
    List<Event> findAll();
}