package com.ticketing.settlement.dto.record;

import com.ticketing.member.domain.SellerGrade;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SettlementDetailRow(Long paymentId,
                                  Long reservationId,
                                  Long sellerId,
                                  Long eventId,
                                  SellerGrade grade,
                                  long grossAmount,
                                  LocalDate settlementDate,
                                  LocalDateTime paidAt) {
}
