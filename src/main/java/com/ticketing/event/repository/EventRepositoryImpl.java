package com.ticketing.event.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.ticketing.event.domain.QEvent.event;

@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Event> search(Category category, String keyword) {

        return queryFactory.selectFrom(event)
                .where(
                        event.status.eq(EventStatus.APPROVED),
                        event.schedules.isNotEmpty(),
                        categoryEq(category),
                        titleContains(keyword)
                )
                .orderBy(event.id.desc())
                .fetch();
    }

    private BooleanExpression categoryEq(Category category) {
        return category != null ? event.category.eq(category) : null;
    }

    private BooleanExpression titleContains(String keyword) {
        return StringUtils.hasText(keyword) ? event.title.contains(keyword) : null;
    }
}
