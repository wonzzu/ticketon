package com.ticketing.reservation;

import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventSchedule;
import com.ticketing.event.domain.EventSeat;
import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.Gender;
import com.ticketing.member.domain.NormalMember;
import com.ticketing.member.domain.Seller;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationSeat;
import com.ticketing.reservation.dto.request.ReservationSearchCond;
import com.ticketing.reservation.service.ReservationService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 성능(N+1) — findMine은 예매 N건마다 schedule/event/venue/seat을 LAZY로 조회한다.
 * 개선(fetch join + batch_size) 전에는 쿼리가 예매 건수에 비례해 폭증(Red),
 * 개선 후에는 건수와 무관한 상수로 줄어든다(Green). 콘솔의 쿼리 수가 before/after 증거.
 */
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)   // DB 정리
@DisplayName("성능 - findMine N+1 쿼리 수")
class ReservationQueryCountTest {

    @Autowired ReservationService reservationService;
    @Autowired TransactionTemplate tx;
    @Autowired EntityManagerFactory emf;
    @PersistenceContext EntityManager em;

    private static final int RESERVATION_COUNT = 10;
    private Long memberId;

    @BeforeEach
    void setup() {
        tx.execute(s -> {
            Seller seller = Seller.create("qc-seller@t.com", "pw", "셀러", "010-0000-0000",
                    new Address("서울", "로1", "00000"), "컴퍼니", "대표", "000-00-00001");
            em.persist(seller);

            NormalMember member = NormalMember.create("qc@t.com", "pw", "회원", "닉",
                    LocalDate.of(1990, 1, 1), Gender.MALE, "010-0000-0001", new Address("서울", "로2", "00000"));
            em.persist(member);
            memberId = member.getId();

            // 예매 N건 — 각각 독립된 venue/event/schedule/seat (1차 캐시로 N+1이 가려지지 않게)
            for (int i = 0; i < RESERVATION_COUNT; i++) {
                Venue venue = Venue.create("홀" + i, new Address("서울", "로" + i, "00000"), 1, 1);
                em.persist(venue);

                Event event = Event.create("공연" + i, "설명", LocalDate.now(), LocalDate.now().plusDays(1),
                        120, "출연", AgeLimit.ALL, Category.CONCERT, "url", seller);
                em.persist(event);

                EventSchedule schedule = EventSchedule.create(venue, LocalDateTime.now().plusDays(1), 1);
                event.addSchedule(schedule);
                em.persist(schedule);

                Seat seat = Seat.of(venue, 1, 1, SeatGrade.VIP);
                em.persist(seat);

                EventSeat eventSeat = EventSeat.create(schedule, seat, 10000);
                em.persist(eventSeat);

                Reservation reservation = Reservation.create(member, schedule, "qc-" + i, 10000);
                reservation.addReservationSeat(ReservationSeat.create(eventSeat, 10000));
                em.persist(reservation);
            }
            em.flush();
            return null;
        });
    }

    @Test
    @DisplayName("findMine 조회 시 실행 쿼리 수 (N+1 → fetch join 후 상수)")
    void findMine_쿼리수() {
        // given : 예매 N건 적재됨. 측정 직전 통계 초기화
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        // when : 내 예매 목록 조회 (DTO 변환까지 서비스 트랜잭션 내에서 수행)
        var page = reservationService.findMine(
                memberId, new ReservationSearchCond(null, null, null), PageRequest.of(0, RESERVATION_COUNT));

        // then : 쿼리 수는 예매 건수에 비례하면 안 된다(상수). N+1이면 건수만큼 폭증
        long queryCount = stats.getPrepareStatementCount();
        System.out.printf("%n========== [findMine N+1] ==========%n" +
                        "  예매 건수      : %d%n" +
                        "  실행 쿼리 수   : %d%n" +
                        "===================================%n",
                RESERVATION_COUNT, queryCount);

        assertThat(page.getContent()).hasSize(RESERVATION_COUNT);
        // 개선 후 예상: content(fetch join) 1 + 컬렉션 batch 1~2 + count 1 ≈ 4~5 → 여유로 6.
        // 정확한 값은 fetch join 적용 후 실측으로 확정. 핵심은 "예매 건수에 비례하지 않는 상수".
        assertThat(queryCount).isLessThanOrEqualTo(6);   // 현재는 N+1이라 Red

    }
}
