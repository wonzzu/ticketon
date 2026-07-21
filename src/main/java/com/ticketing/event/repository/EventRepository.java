package com.ticketing.event.repository;

import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>,EventRepositoryCustom {
    List<Event> findByStatus(EventStatus status);

    List<Event> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}
