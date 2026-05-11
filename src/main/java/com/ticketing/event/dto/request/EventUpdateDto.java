package com.ticketing.event.dto.request;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class EventUpdateDto {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private Integer runningTime;

    @NotBlank
    private String cast;

}
