package com.ticketing.event.dto.request;

import com.ticketing.event.domain.GradePrice;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class EventScheduleCreateDto {

    @NotNull
    private Long venueId;

    @NotNull
    private LocalDateTime showDateTime;

    @NotEmpty
    private List<GradePrice> gradePrices;
}
