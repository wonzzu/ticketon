package com.ticketing.event.service;

import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventSchedule;
import com.ticketing.event.domain.EventSeat;
import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.Seller;
import com.ticketing.venue.domain.Seat;
import com.ticketing.venue.domain.SeatGrade;
import com.ticketing.venue.domain.Venue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 성능(N+1) — findByScheduleId는 좌석마다 EventSeatResponseDto.from()에서 seat(Seat)을 LAZY로 조회한다.
 * 개선(fetch join seat) 전에는 쿼리가 좌석 개수에 비례해 폭증(Red),
 * 개선 후에는 개수와 무관한 상수로 줄어든다(Green). 콘솔의 쿼리 수가 before/after 증거.
 */
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)   // DB 정리
@DisplayName("성능 - 좌석 목록 N+1 쿼리 수")
class EventSeatQueryCountTest {

    @Autowired EventSeatService eventSeatService;
    @Autowired TransactionTemplate tx;
    @Autowired EntityManagerFactory emf;
    @PersistenceContext EntityManager em;

    private static final int SEAT_COUNT = 100;
    private Long scheduleId;

    @BeforeEach
    void setup() {
        tx.execute(s -> {
            Seller seller = Seller.create("seat-seller@t.com", "pw", "셀러", "010-0000-0000",
                    new Address("서울", "로1", "00000"), "컴퍼니", "대표", "000-00-00001");
            em.persist(seller);

            Venue venue = Venue.create("좌석홀", new Address("서울", "로2", "00000"), SEAT_COUNT, 1);
            em.persist(venue);

            Event event = Event.create("좌석공연", "설명", LocalDate.now(), LocalDate.now().plusDays(1),
                    120, "출연", AgeLimit.ALL, Category.CONCERT, "url", seller);
            em.persist(event);

            EventSchedule schedule = EventSchedule.create(venue, LocalDateTime.now().plusDays(1), 1);
            event.addSchedule(schedule);
            em.persist(schedule);
            scheduleId = schedule.getId();

            // 좌석 N개 — 각 EventSeat가 서로 다른 Seat 참조 (from()에서 seat LAZY 조회 → N+1)
            for (int i = 0; i < SEAT_COUNT; i++) {
                Seat seat = Seat.of(venue, i + 1, 1, SeatGrade.VIP);
                em.persist(seat);
                em.persist(EventSeat.create(schedule, seat, 10000));
            }
            em.flush();
            return null;
        });
    }

    @Test
    @DisplayName("findByScheduleId 조회 시 실행 쿼리 수 (좌석마다 Seat LAZY → N+1)")
    void 좌석목록_쿼리수() {
        // given : 좌석 N개 적재됨. 측정 직전 통계 초기화
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        // when : 스케줄 좌석 목록 조회
        var result = eventSeatService.findByScheduleId(scheduleId);

        // then : 쿼리 수는 좌석 개수에 비례하면 안 된다(상수). N+1이면 개수만큼 폭증
        long queryCount = stats.getPrepareStatementCount();
        System.out.printf("%n========== [좌석 목록 N+1] ==========%n" +
                        "  좌석 개수      : %d%n" +
                        "  실행 쿼리 수   : %d%n" +
                        "====================================%n",
                SEAT_COUNT, queryCount);

        assertThat(result).hasSize(SEAT_COUNT);
        assertThat(queryCount).isLessThanOrEqualTo(3);   // 개선 후 목표(상수). 현재는 N+1이라 Red
    }
}
