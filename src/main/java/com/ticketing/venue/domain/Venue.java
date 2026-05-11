package com.ticketing.venue.domain;

import com.ticketing.global.entity.Address;
import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLRestriction("deleted_at IS NULL")
public class Venue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Embedded
    private Address address;

    @Column(nullable = false)
    private Integer totalCapacity;

    @Column
    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(mappedBy = "venue")
    private List<Seat> seats = new ArrayList<>();

    public static Venue create(String name, Address address, int rowCount, int columnCount) {
        return Venue.builder()
                .name(name)
                .address(address)
                .totalCapacity(rowCount * columnCount)
                .build();
    }

    public void update(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public List<Seat> assignSeats(int rowCount, int columnCount, List<SeatGradeRange> ranges) {
        List<Seat> result = new ArrayList<>();
        for (int row = 1; row <= rowCount; row++) {
            SeatGrade grade = resolveGrade(row, ranges);
            for (int col = 1; col <= columnCount; col++) {
                result.add(Seat.of(this, row, col, grade));
            }
        }
        return result;
    }

    private SeatGrade resolveGrade(int row, List<SeatGradeRange> ranges) {
        return ranges.stream()
                .filter(r -> row >= r.fromRow() && row <= r.toRow())
                .map(SeatGradeRange::grade)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("등급 범위에 포함안되는 행입니다."));
    }
}
