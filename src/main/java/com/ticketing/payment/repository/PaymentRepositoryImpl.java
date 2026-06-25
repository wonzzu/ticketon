package com.ticketing.payment.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.payment.domain.PaymentStatus;
import com.ticketing.payment.dto.EventOrderCount;
import com.ticketing.payment.dto.PaymentSalesAggregate;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.ticketing.payment.domain.QPayment.payment;
import static com.ticketing.reservation.domain.QReservation.reservation;

@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PaymentSalesAggregate aggregatePaid(LocalDateTime start, LocalDateTime end) {

        return queryFactory
                .select(Projections.constructor(PaymentSalesAggregate.class
                        , payment.count()
                        , payment.amount.sum().coalesce(0).longValue()))
                .from(payment)
                .where(payment.status.eq(PaymentStatus.PAID),
                        createdBetween(start, end)
                ).fetchOne();

    }

    @Override
    public List<EventOrderCount> aggregatePaidByEvent(LocalDateTime start, LocalDateTime end) {

        return queryFactory
                .select(Projections.constructor(EventOrderCount.class,
                        reservation.eventSchedule.event.id,
                        payment.count()))
                .from(payment)
                .join(payment.reservation, reservation)
                .where(payment.status.eq(PaymentStatus.PAID), createdBetween(start, end))
                .groupBy(reservation.eventSchedule.event.id)
                .fetch();
    }


    private BooleanExpression createdBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return null;
        return payment.createdAt.goe(from).and(payment.createdAt.lt(to));
    }
}
