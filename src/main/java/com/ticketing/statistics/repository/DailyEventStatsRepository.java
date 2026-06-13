package com.ticketing.statistics.repository;

import com.ticketing.statistics.domain.DailyEventStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyEventStatsRepository extends JpaRepository<DailyEventStats, Long> {

    void deleteByStatDate(LocalDate statDate);

    @Query("select d.eventId from DailyEventStats d where d.statDate >= :from " +
            "group by d.eventId order by sum(d.orderCount) DESC")
    List<Long> findRankedEventIds(@Param("from") LocalDate from, Pageable pageable);

}
