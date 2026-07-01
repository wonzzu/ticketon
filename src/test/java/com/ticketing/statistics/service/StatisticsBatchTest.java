package com.ticketing.statistics.service;

import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventSchedule;
import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.Gender;
import com.ticketing.member.domain.NormalMember;
import com.ticketing.member.domain.Seller;
import com.ticketing.payment.domain.Payment;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.statistics.domain.DailySalesStats;
import com.ticketing.statistics.repository.DailyEventStatsRepository;
import com.ticketing.statistics.repository.DailySalesStatsRepository;
import com.ticketing.venue.domain.Venue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
 * 통계 배치 멱등성 — aggregateDaily를 2번 실행해도
 * (1) 행이 중복 적재되지 않고, (2) 값이 누적되지 않는다(DELETE&INSERT 방식).
 * 만약 `UPDATE SET col = col + x` 누적 방식이었다면 2배로 뻥튀기됐을 것 → 그 회귀를 잡는다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)   // DB 정리
@DisplayName("통계 - 일별 배치 멱등성")
class StatisticsBatchTest {

    @Autowired StatisticsService statisticsService;
    @Autowired DailySalesStatsRepository dailySalesStatsRepository;
    @Autowired DailyEventStatsRepository dailyEventStatsRepository;
    @Autowired TransactionTemplate tx;
    @PersistenceContext EntityManager em;

    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setup() {
        // 오늘자 결제(PAID) 1건만 적재. aggregatePaid는 payment.status=PAID + createdAt(오늘) 기준이라
        // 결제 서비스/대기열 흐름 없이 엔티티 persist만으로 집계 소스가 갖춰진다.
        tx.execute(s -> {
            Seller seller = Seller.create("stat-seller@t.com", "pw", "셀러", "010-0000-0000",
                    new Address("서울", "로1", "00000"), "컴퍼니", "대표", "000-00-00001");
            em.persist(seller);

            NormalMember member = NormalMember.create("stat@t.com", "pw", "회원", "닉",
                    LocalDate.of(1990, 1, 1), Gender.MALE, "010-0000-0001", new Address("서울", "로2", "00000"));
            em.persist(member);

            Venue venue = Venue.create("통계홀", new Address("서울", "로3", "00000"), 1, 1);
            em.persist(venue);

            Event event = Event.create("통계공연", "설명", today, today.plusDays(1),
                    120, "출연", AgeLimit.ALL, Category.CONCERT, "url", seller);
            em.persist(event);

            EventSchedule schedule = EventSchedule.create(venue, LocalDateTime.now().plusDays(1), 1);
            event.addSchedule(schedule);
            em.persist(schedule);

            Reservation reservation = Reservation.create(member, schedule, "idem-stat-1", 10000);
            em.persist(reservation);

            em.persist(Payment.paid(reservation, 10000));   // createdAt=now(오늘) 자동, status=PAID

            em.flush();
            return null;
        });
    }

    @Test
    @DisplayName("aggregateDaily 2회 실행 → 행 1개 유지 + 값 누적 안 됨")
    void 배치_멱등() {
        // given : setup에서 오늘자 PAID 결제 1건

        // when : 같은 날짜를 두 번 집계
        statisticsService.aggregateDaily(today);
        statisticsService.aggregateDaily(today);

        // then : DELETE&INSERT라 행이 중복되지도, 값이 누적되지도(2배) 않는다
        assertThat(dailySalesStatsRepository.count()).isEqualTo(1);
        assertThat(dailyEventStatsRepository.count()).isEqualTo(1);

        DailySalesStats stats = dailySalesStatsRepository.findAll().get(0);
        assertThat(stats.getOrderCount()).isEqualTo(1L);
        assertThat(stats.getSalesAmount()).isEqualTo(10000L);
    }
}
