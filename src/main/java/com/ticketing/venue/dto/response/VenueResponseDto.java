package com.ticketing.venue.dto.response;

import com.ticketing.global.entity.Address;
import com.ticketing.venue.domain.Venue;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VenueResponseDto {
    private Long id;

    private String name;

    private Address address;

    private Integer totalCapacity;


    public static VenueResponseDto from(Venue venue) {

        return VenueResponseDto.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .totalCapacity(venue.getTotalCapacity())
                .build();
    }

}
