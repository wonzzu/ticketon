package com.ticketing.reservation.dto.response;

import com.ticketing.event.domain.EventSchedule;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationSeat;
import com.ticketing.reservation.domain.ReservationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReservationResponseDto {

    private Long id;
    private String eventTitle;
    private LocalDateTime showDateTime;
    private String venueName;
    private Integer totalPrice;
    private ReservationStatus status;
    private String statusLabel;
    private List<SeatInfo> seats;
    private LocalDateTime createdAt;

    public static ReservationResponseDto from(Reservation reservation) {

        EventSchedule schedule = reservation.getEventSchedule();
        return ReservationResponseDto.builder()
                .id(reservation.getId())
                .eventTitle(schedule.getEvent().getTitle())
                .showDateTime(schedule.getShowDateTime())
                .venueName(schedule.getVenue().getName())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .statusLabel(reservation.getStatus().getDescription())
                .seats(reservation.getReservationSeats().stream()
                        .map(reservationSeat -> SeatInfo.builder()
                                .seatRow(reservationSeat.getEventSeat().getSeat().getSeatRow())
                                .seatColumn(reservationSeat.getEventSeat().getSeat().getSeatColumn())
                                .price(reservationSeat.getPrice())
                                .build()).toList())
                .createdAt(reservation.getCreatedAt())
                .build();
    }


    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class SeatInfo {
        private Integer seatRow;
        private Integer seatColumn;
        private Integer price;
    }
}
