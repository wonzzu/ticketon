package com.ticketing.review.repository;

import com.ticketing.event.domain.EventStatus;

import java.util.List;

public interface ReviewRepositoryCustom {

    List<Long> findTopRatedEventIds(EventStatus status, long minReviews, int limit);

}
