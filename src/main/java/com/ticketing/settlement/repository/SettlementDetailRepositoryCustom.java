package com.ticketing.settlement.repository;

import com.ticketing.settlement.domain.SettlementDetail;
import com.ticketing.settlement.dto.request.SettlementDetailSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettlementDetailRepositoryCustom {

    Page<SettlementDetail> search(Long sellerId, SettlementDetailSearchCond cond, Pageable pageable);
}
