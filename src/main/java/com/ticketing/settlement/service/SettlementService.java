package com.ticketing.settlement.service;

import com.ticketing.event.domain.Event;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.global.exception.BaseException;
import com.ticketing.settlement.domain.Settlement;
import com.ticketing.settlement.domain.SettlementDetail;
import com.ticketing.settlement.dto.request.SettlementDetailSearchCond;
import com.ticketing.settlement.dto.response.SettlementDetailLineResponseDto;
import com.ticketing.settlement.dto.response.SettlementDetailSearchResponseDto;
import com.ticketing.settlement.dto.response.SettlementResponseDto;
import com.ticketing.settlement.repository.SettlementDetailRepository;
import com.ticketing.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

import static com.ticketing.global.baseresponse.BaseResponseStatus.SETTLEMENT_NOT_FOUND;
import static com.ticketing.global.baseresponse.BaseResponseStatus.SETTLEMENT_NOT_OWNED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;
    private final EventRepository eventRepository;

    public Page<SettlementResponseDto> findMySettlements(Long sellerId, Pageable pageable) {
        Page<Settlement> page = settlementRepository.findBySellerIdOrderBySettlementDateDesc(sellerId, pageable);

        Map<Long, String> titleMap = eventRepository.findAllById(
                        page.getContent().stream().map(Settlement::getEventId).toList())
                .stream().collect(Collectors.toMap(Event::getId, Event::getTitle));

        return page.map(s -> SettlementResponseDto.of(
                s, titleMap.getOrDefault(s.getEventId(), "(삭제된 공연)")));
    }

    public Page<SettlementDetailLineResponseDto> findMySettlementDetails(
            Long settlementId, Long sellerId, Pageable pageable) {

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BaseException(SETTLEMENT_NOT_FOUND));

        if (!settlement.getSellerId().equals(sellerId)) {
            throw new BaseException(SETTLEMENT_NOT_OWNED);
        }

        return settlementDetailRepository.findBySellerIdAndEventIdAndSettlementDate(
                        sellerId, settlement.getEventId(), settlement.getSettlementDate(), pageable)
                .map(SettlementDetailLineResponseDto::from);
    }

    public Page<SettlementDetailSearchResponseDto> searchMyDetails(
            Long sellerId, SettlementDetailSearchCond cond, Pageable pageable) {

        Page<SettlementDetail> page = settlementDetailRepository.search(sellerId, cond, pageable);

        Map<Long, String> titleMap = eventRepository.findAllById(
                        page.getContent().stream().map(SettlementDetail::getEventId).distinct().toList())
                .stream().collect(Collectors.toMap(Event::getId, Event::getTitle));

        return page.map(d -> SettlementDetailSearchResponseDto.of(
                d, titleMap.getOrDefault(d.getEventId(), "(삭제된 공연)")));
    }
}