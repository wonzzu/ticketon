package com.ticketing.review.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.event.domain.EventStatus;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.ticketing.review.domain.QReview.*;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findTopRatedEventIds(EventStatus status, long minReviews, int limit) {

        return queryFactory
                .select(review.event.id)
                .from(review)
                .where(review.event.status.eq(status))
                .groupBy(review.event.id)
                .having(review.count().goe(minReviews))
                .orderBy(review.rating.avg().desc(), review.count().desc())
                .limit(limit)
                .fetch();
    }
}
