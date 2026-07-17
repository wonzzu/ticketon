package com.ticketing.settlement.repository;

import com.ticketing.settlement.domain.SettlementDirtyDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementDirtyDateRepository extends JpaRepository<SettlementDirtyDate, Long> {
}