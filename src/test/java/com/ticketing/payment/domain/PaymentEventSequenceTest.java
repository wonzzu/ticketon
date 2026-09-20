package com.ticketing.payment.domain;

import com.ticketing.reservation.domain.Reservation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("Payment 이벤트 순번")
class PaymentEventSequenceTest {

    @Test
    @DisplayName("동일 Payment에서 발생한 이벤트 순번을 1부터 차례로 발급한다")
    void issueSequenceInOrder() {
        Payment payment = Payment.paid(mock(Reservation.class), 10_000);

        assertThat(payment.nextEventSequence()).isEqualTo(1L);
        assertThat(payment.nextEventSequence()).isEqualTo(2L);
        assertThat(payment.getEventSequence()).isEqualTo(2L);
    }
}
