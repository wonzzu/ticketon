package com.ticketing.settlement.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.settlement.domain.SettlementDetail;
import com.ticketing.settlement.dto.request.SettlementDetailSearchCond;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.ticketing.settlement.domain.QSettlementDetail.settlementDetail;

@RequiredArgsConstructor
public class SettlementDetailRepositoryImpl implements SettlementDetailRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SettlementDetail> search(Long sellerId, SettlementDetailSearchCond cond, Pageable pageable) {

        List<SettlementDetail> result = queryFactory
                .selectFrom(settlementDetail)
                .where(
                        settlementDetail.sellerId.eq(sellerId),
                        paymentIdEq(cond.paymentId()),
                        reservationIdEq(cond.reservationId()),
                        eventIdEq(cond.eventId()),
                        paidGoe(cond.paidFrom()),
                        paidLt(cond.paidTo())
                )
                .orderBy(settlementDetail.settlementDate.desc(), settlementDetail.paymentId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(settlementDetail.count())
                .from(settlementDetail)
                .where(
                        settlementDetail.sellerId.eq(sellerId),
                        paymentIdEq(cond.paymentId()),
                        reservationIdEq(cond.reservationId()),
                        eventIdEq(cond.eventId()),
                        paidGoe(cond.paidFrom()),
                        paidLt(cond.paidTo())
                );

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    private BooleanExpression paymentIdEq(Long paymentId) {
        return paymentId != null ? settlementDetail.paymentId.eq(paymentId) : null;
    }

    private BooleanExpression reservationIdEq(Long reservationId) {
        return reservationId != null ? settlementDetail.reservationId.eq(reservationId) : null;
    }

    private BooleanExpression eventIdEq(Long eventId) {
        return eventId != null ? settlementDetail.eventId.eq(eventId) : null;
    }

    // 결제일 기간 — 날짜로 받아 그날 00:00부터 / 종료일 다음날 00:00 직전까지(종료일 포함)
    private BooleanExpression paidGoe(LocalDate from) {
        return from != null ? settlementDetail.paidAt.goe(from.atStartOfDay()) : null;
    }

    private BooleanExpression paidLt(LocalDate to) {
        return to != null ? settlementDetail.paidAt.lt(to.plusDays(1).atStartOfDay()) : null;
    }
}
