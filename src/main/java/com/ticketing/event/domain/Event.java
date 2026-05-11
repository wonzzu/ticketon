package com.ticketing.event.domain;

import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLRestriction("deleted_at IS NULL")
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer runningTime;

    private String cast;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgeLimit ageLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column
    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventSchedule> schedules = new ArrayList<>();



    //연관관계 편의 메서드
    public void addSchedule(EventSchedule schedule) {
        this.schedules.add(schedule);
        schedule.setEvent(this);
    }

    public static Event create(String title, String description, LocalDate startDate, LocalDate endDate, Integer runningTime,
                               String cast, AgeLimit ageLimit, Category category) {
        validatePeriod(startDate, endDate);
        return Event.builder()
                .title(title)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .runningTime(runningTime)
                .cast(cast)
                .ageLimit(ageLimit)
                .category(category)
                .build();
    }

    public void update(String title, String description, String cast,
                       Integer runningTime, LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);

        this.title = title;
        this.description = description;
        this.cast = cast;
        this.runningTime = runningTime;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    private static void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("종료일이 시작일보다 빠를 수 없습니다.");
        }
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
