package com.ticketing.venue.dto.request;

import com.ticketing.venue.domain.SeatGrade;
import com.ticketing.venue.domain.SeatGradeRange;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GradeRangeDto {
    @NotNull
    private SeatGrade seatGrade;

    @NotNull
    private Integer toRow;

    @NotNull
    private Integer fromRow;

    public SeatGradeRange toGradeRange() {
        return new SeatGradeRange(seatGrade, fromRow, toRow);
    }
}
