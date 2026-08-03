package com.ticketing.statistics.repository;

import com.ticketing.statistics.domain.StatsDirtyDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface StatsDirtyDateRepository extends JpaRepository<StatsDirtyDate, Long> {

    boolean existsByStatDate(LocalDate statDate);
}
