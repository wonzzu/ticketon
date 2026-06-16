package com.ticketing.event.repository;

import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;

import java.util.List;

public interface EventRepositoryCustom {
    List<Event> search(Category category, String keyword);
}
