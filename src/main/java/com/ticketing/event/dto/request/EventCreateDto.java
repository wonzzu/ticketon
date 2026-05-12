package com.ticketing.event.dto.request;

import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class EventCreateDto {

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

    @NotBlank
    private AgeLimit ageLimit;

    @NotBlank
    private Category category;

    // 포스터 이미지 URL — 외부 호스팅(picsum 등) 또는 추후 S3 URL. 빈 값 허용.
    private String posterUrl;
}
