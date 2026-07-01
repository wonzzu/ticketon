package com.ticketing.event.repository;

import com.ticketing.event.domain.EventSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventSeatRepository extends JpaRepository<EventSeat, Long> {

    @Query("select es from EventSeat es join fetch es.seat where es.eventSchedule.id = :scheduleId")
    List<EventSeat> findByEventScheduleId(Long scheduleId);
}
