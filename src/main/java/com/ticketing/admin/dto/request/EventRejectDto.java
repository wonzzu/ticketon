package com.ticketing.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EventRejectDto {

    @NotBlank
    private String reason;
}
