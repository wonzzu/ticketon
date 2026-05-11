package com.ticketing.event.repository;

import com.ticketing.event.domain.EventSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventSeatRepository extends JpaRepository<EventSeat, Long> {

    //TODO N+1 문제 발생해서 이거 나중에 fetch Join 으로 가져오게 바꿔야함 지금은 성능 테스트 위해 냅두기
    List<EventSeat> findByEventScheduleId(Long scheduleId);
}
