package com.ticketing.statistics.repository;

import com.ticketing.statistics.domain.DailySalesStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailySalesStatsRepository extends JpaRepository<DailySalesStats, Long> {

    void deleteByStatDate(LocalDate statDate);

    boolean existsByStatDate(LocalDate statDate);

    List<DailySalesStats> findByStatDateBetweenOrderByStatDate(LocalDate from, LocalDate to);
}
