package com.ticketing.payment.repository;

import com.ticketing.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReservationId(Long reservationId);

    boolean existsByReservationId(Long reservationId);

}
