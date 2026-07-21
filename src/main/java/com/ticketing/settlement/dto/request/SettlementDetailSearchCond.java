package com.ticketing.settlement.dto.request;

import java.time.LocalDate;

// 건별 명세 추적 검색 조건 — 전부 optional (null이면 해당 조건 무시)
// paidFrom/paidTo = 고객 결제일 기간 (공연 조건과 조합해 "이 공연에서 N월에 팔린 건" 조회)
public record SettlementDetailSearchCond(Long paymentId,
                                         Long reservationId,
                                         Long eventId,
                                         LocalDate paidFrom,
                                         LocalDate paidTo) {
}
