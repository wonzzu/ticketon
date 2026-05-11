package com.ticketing.event.dto.response;

import com.ticketing.event.domain.EventSeat;
import com.ticketing.event.domain.EventSeatStatus;
import com.ticketing.venue.domain.SeatGrade;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventSeatResponseDto {


    private Long id;

    private Integer seatRow;

    private Integer seatColumn;

    private SeatGrade grade;

    private EventSeatStatus status;

    private Integer price;

    public static EventSeatResponseDto from(EventSeat eventSeat) {
        return EventSeatResponseDto.builder()
                .id(eventSeat.getId())
                .seatRow(eventSeat.getSeat().getSeatRow())
                .seatColumn(eventSeat.getSeat().getSeatColumn())
                .grade(eventSeat.getSeat().getSeatGrade())
                .status(eventSeat.getStatus())
                .price(eventSeat.getPrice())
                .build();
    }
}
