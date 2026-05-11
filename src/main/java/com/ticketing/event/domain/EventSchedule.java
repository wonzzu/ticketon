package com.ticketing.event.domain;

import com.ticketing.global.entity.BaseEntity;
import com.ticketing.venue.domain.Venue;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_event_schedule"
        , columnNames = {"event_id", "venue_id", "round_number"}))
public class EventSchedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private LocalDateTime showDateTime;

    @Column(nullable = false)
    private Integer roundNumber;

    @Builder.Default
    @OneToMany(mappedBy = "eventSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventSeat> eventSeats = new ArrayList<>();

    //양방향 동기화용.
    void setEvent(Event event) {
        this.event = event;
    }

    public void addEventSeat(EventSeat eventSeat) {
        this.eventSeats.add(eventSeat);
        eventSeat.setSchedule(this);
    }


    public static EventSchedule create(Venue venue, LocalDateTime showDateTime, int roundNumber) {

        return EventSchedule.builder()
                .venue(venue)
                .showDateTime(showDateTime)
                .roundNumber(roundNumber)
                .build();
    }
}
