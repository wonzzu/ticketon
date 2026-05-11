package com.ticketing.venue.dto.request;

import com.ticketing.global.entity.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class VenueUpdateDto {

    @NotBlank
    private String name;

    @NotNull
    private Address address;

}
