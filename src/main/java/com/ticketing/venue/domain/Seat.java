package com.ticketing.venue.domain;


import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"venue_id", "seat_row", "seat_column"}))
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatGrade seatGrade;

    @Column(nullable = false)
    private Integer seatRow;

    @Column(nullable = false)
    private Integer seatColumn;

    public static Seat of(Venue venue, int row, int column, SeatGrade grade) {
        return Seat.builder()
                .venue(venue)
                .seatRow(row)
                .seatColumn(column)
                .seatGrade(grade)
                .build();
    }

}
