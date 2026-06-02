package com.ticketing.review.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
public class ReviewCreateDto {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotBlank
    @Size(max = 1000)
    private String content;
}

