package com.ticketing.payment.repository;

import com.ticketing.payment.domain.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findByPaymentIdOrderByCreatedAtAsc(Long paymentId);
}
