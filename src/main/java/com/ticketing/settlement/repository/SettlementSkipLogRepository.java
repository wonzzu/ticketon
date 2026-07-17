package com.ticketing.settlement.repository;

import com.ticketing.settlement.domain.SettlementSkipLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementSkipLogRepository extends JpaRepository<SettlementSkipLog,Long> {
}
