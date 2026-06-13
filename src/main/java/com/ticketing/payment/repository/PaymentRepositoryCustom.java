package com.ticketing.payment.repository;

import com.ticketing.payment.dto.PaymentSalesAggregate;

import java.time.LocalDateTime;

public interface PaymentRepositoryCustom {

    PaymentSalesAggregate aggregatePaid(LocalDateTime start, LocalDateTime end);
}
