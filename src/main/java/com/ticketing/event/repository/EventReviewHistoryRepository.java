package com.ticketing.event.repository;

import com.ticketing.event.domain.EventReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventReviewHistoryRepository extends JpaRepository<EventReviewHistory, Long> {

    List<EventReviewHistory> findByEventIdOrderByCreatedAtDesc(Long eventId);
}
