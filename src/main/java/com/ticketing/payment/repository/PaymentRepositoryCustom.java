package com.ticketing.payment.repository;

import com.ticketing.payment.dto.EventOrderCount;
import com.ticketing.payment.dto.PaymentSalesAggregate;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepositoryCustom {

    PaymentSalesAggregate aggregatePaid(LocalDateTime start, LocalDateTime end);

    List<EventOrderCount> aggregatePaidByEvent(LocalDateTime start, LocalDateTime end);
}
