package com.ticketing.settlement.dto;

import com.ticketing.member.domain.SellerGrade;

import java.time.LocalDate;

public record SettlementAggregateDto(Long sellerId, Long eventId, LocalDate settlementDate, long grossAmount
                                    , SellerGrade grade) {
}
