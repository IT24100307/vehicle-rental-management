package com.system.project1.repository;

import com.system.project1.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByIsActiveTrue();

    List<Event> findByEventType(String eventType);
}
