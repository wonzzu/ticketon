package com.ticketing.reservation.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.event.domain.QEvent;
import com.ticketing.event.domain.QEventSchedule;
import com.ticketing.reservation.domain.QReservation;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.reservation.dto.request.ReservationSearchCond;
import com.ticketing.venue.domain.QVenue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.ticketing.event.domain.QEvent.*;
import static com.ticketing.event.domain.QEventSchedule.*;
import static com.ticketing.reservation.domain.QReservation.*;
import static com.ticketing.venue.domain.QVenue.*;

@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Reservation> search(Long memberId, ReservationSearchCond cond, Pageable pageable) {

        //content
        List<Reservation> result = queryFactory.selectFrom(reservation)
                .leftJoin(reservation.eventSchedule, eventSchedule).fetchJoin()
                .leftJoin(eventSchedule.event, event).fetchJoin()
                .leftJoin(eventSchedule.venue, venue).fetchJoin()
                .where(reservation.member.id.eq(memberId),
                        statusEq(cond.status()),
                        createdGoe(cond.from()),
                        createdLt(cond.to()))
                .orderBy(reservation.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //count
        JPAQuery<Long> count = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(reservation.member.id.eq(memberId),
                        statusEq(cond.status()),
                        createdGoe(cond.from()),
                        createdLt(cond.to()));

        return PageableExecutionUtils.getPage(result, pageable, count::fetchOne);
    }


    private BooleanExpression statusEq(ReservationStatus status) {
        return status != null ? reservation.status.eq(status) : null;
    }

    private BooleanExpression createdGoe(LocalDate from) {
        return from != null ? reservation.createdAt.goe(from.atStartOfDay()) : null;
    }

    private BooleanExpression createdLt(LocalDate to) {
        return to != null ? reservation.createdAt.lt(to.plusDays(1).atStartOfDay()) : null;
    }

}


