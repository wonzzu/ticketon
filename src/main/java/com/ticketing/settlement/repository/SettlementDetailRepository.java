package com.ticketing.settlement.repository;

import com.ticketing.settlement.domain.SettlementDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long>, SettlementDetailRepositoryCustom {

    Page<SettlementDetail> findBySellerIdAndEventIdAndSettlementDate(
            Long sellerId, Long eventId, LocalDate settlementDate, Pageable pageable);

}
