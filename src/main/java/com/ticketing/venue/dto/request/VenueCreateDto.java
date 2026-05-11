package com.ticketing.venue.dto.request;

import com.ticketing.global.entity.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class VenueCreateDto {

    @NotBlank
    private String name;

    @NotNull
    private Address address;


    @NotNull
    private Integer rowCount;

    @NotNull
    private Integer columnCount;

    @NotNull
    private List<GradeRangeDto> rangeDtoList;


}
