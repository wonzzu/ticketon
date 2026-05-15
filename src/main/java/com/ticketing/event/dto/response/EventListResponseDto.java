package com.ticketing.event.dto.response;

import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class EventListResponseDto {

    private Long id;

    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    private AgeLimit ageLimit;

    private Category category;

    private String posterUrl;

    private EventStatus status;

    private String statusLabel;

    public static EventListResponseDto from(Event event) {
       return EventListResponseDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .ageLimit(event.getAgeLimit())
                .category(event.getCategory())
                .posterUrl(event.getPosterUrl())
                .status(event.getStatus())
                .statusLabel(event.getStatus().getLabel())
                .build();
    }
}
