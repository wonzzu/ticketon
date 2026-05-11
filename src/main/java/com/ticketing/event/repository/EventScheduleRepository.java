package com.ticketing.event.repository;

import com.ticketing.event.domain.EventSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventScheduleRepository extends JpaRepository<EventSchedule, Long> {

    int countByEventIdAndVenueId(Long eventId, Long venueId);

    List<EventSchedule> findByEventIdOrderByShowDateTimeAsc(Long eventId);
}
