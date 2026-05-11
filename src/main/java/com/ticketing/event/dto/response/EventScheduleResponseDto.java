package com.ticketing.event.dto.response;

import com.ticketing.event.domain.EventSchedule;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class EventScheduleResponseDto {

    private Long id;

    private Long eventId;

    private Long venueId;

    private String venueName;

    private LocalDateTime showDateTime;

    private Integer roundNumber;


    public static EventScheduleResponseDto from(EventSchedule eventSchedule) {

        return EventScheduleResponseDto.builder()
                .id(eventSchedule.getId())
                .eventId(eventSchedule.getEvent().getId())
                .venueId(eventSchedule.getVenue().getId())
                .venueName(eventSchedule.getVenue().getName())
                .showDateTime(eventSchedule.getShowDateTime())
                .roundNumber(eventSchedule.getRoundNumber())
                .build();
    }
}
