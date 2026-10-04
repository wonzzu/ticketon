package com.ticketing.kafka.failure.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.kafka.failure.domain.FailedKafkaEvent;
import com.ticketing.kafka.failure.domain.FailedKafkaEventStatus;
import com.ticketing.kafka.failure.dto.request.FailedKafkaEventSearchCondition;
import com.ticketing.outbox.domain.OutboxConsumerType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.ticketing.kafka.failure.domain.QFailedKafkaEvent.failedKafkaEvent;

@RequiredArgsConstructor
public class FailedKafkaEventRepositoryImpl implements FailedKafkaEventRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<FailedKafkaEvent> search(
            FailedKafkaEventSearchCondition condition,
            Pageable pageable
    ) {
        List<FailedKafkaEvent> content = queryFactory
                .selectFrom(failedKafkaEvent)
                .where(
                        statusEq(condition.status()),
                        consumerTypeEq(condition.consumerType()),
                        originalTopicEq(condition.originalTopic()),
                        eventIdContains(condition.eventId())
                )
                .orderBy(failedKafkaEvent.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(failedKafkaEvent.count())
                .from(failedKafkaEvent)
                .where(
                        statusEq(condition.status()),
                        consumerTypeEq(condition.consumerType()),
                        originalTopicEq(condition.originalTopic()),
                        eventIdContains(condition.eventId())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression statusEq(FailedKafkaEventStatus status) {
        return status != null ? failedKafkaEvent.status.eq(status) : null;
    }

    private BooleanExpression consumerTypeEq(OutboxConsumerType consumerType) {
        return consumerType != null ? failedKafkaEvent.consumerType.eq(consumerType) : null;
    }

    private BooleanExpression originalTopicEq(String originalTopic) {
        return StringUtils.hasText(originalTopic)
                ? failedKafkaEvent.originalTopic.eq(originalTopic)
                : null;
    }

    private BooleanExpression eventIdContains(String eventId) {
        return StringUtils.hasText(eventId)
                ? failedKafkaEvent.eventId.contains(eventId)
                : null;
    }
}
