package com.ticketing.settlement.repository;

import com.ticketing.settlement.domain.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement,Long> {

    boolean existsByEventId(Long eventId);

    Page<Settlement> findBySellerIdOrderBySettlementDateDesc(Long sellerId, Pageable pageable);
}
