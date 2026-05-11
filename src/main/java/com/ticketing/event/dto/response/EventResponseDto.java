package com.ticketing.event.dto.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import lombok.*;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class EventResponseDto {

    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer runningTime;

    private String cast;

    private AgeLimit ageLimit;

    private Category category;


    public static EventResponseDto from(Event event) {
        return EventResponseDto.builder()
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .runningTime(event.getRunningTime())
                .cast(event.getCast())
                .ageLimit(event.getAgeLimit())
                .category(event.getCategory())
                .build();
    }

}

