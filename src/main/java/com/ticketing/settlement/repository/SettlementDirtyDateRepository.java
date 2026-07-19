package com.ticketing.settlement.repository;

import com.ticketing.settlement.domain.SettlementDirtyDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface SettlementDirtyDateRepository extends JpaRepository<SettlementDirtyDate, Long> {

    boolean existsBySellerIdAndEventIdAndSettlementDate(Long sellerId, Long eventId, LocalDate settlementDate);
}