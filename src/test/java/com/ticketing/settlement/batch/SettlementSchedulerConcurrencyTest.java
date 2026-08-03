package com.ticketing.settlement.batch;

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
import com.ticketing.venue.domain.Venue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * 서버를 여러 대로 늘리면 @Scheduled 가 인스턴스마다 각자 실행된다.
 * 정산은 날짜 단위 DELETE & INSERT 라 순차 재실행은 안전하지만, 동시 실행은 서로를 밟는다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("정산 배치 - 다중 인스턴스 중복 실행")
class SettlementSchedulerConcurrencyTest {

    private static final int INSTANCES = 3;
    private static final int PRICE = 100_000;

    //백엔드 서버 3대에서 실제로 배치를 돌린 시간 (BATCH_JOB_EXECUTION)
    //   13:55:07.359 / 13:55:07.391 / 13:55:07.404
    // cron 같아 같은 시간에 실행되도 서버마다 지연생겨 ms가 조금씩 다름. 간격을 재현.
    private static final long[] START_OFFSET_MS = {0, 32, 45};

    @Autowired SettlementScheduler settlementScheduler;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired TransactionTemplate tx;
    @PersistenceContext EntityManager em;

    // 실행 '시도'를 세기 위한 스파이 — 데드락으로 실패한 실행도 시도로 잡힌다
    @MockitoSpyBean JobLauncher jobLauncher;

    private final LocalDate settlementDate = LocalDate.now().minusDays(1);

    @BeforeEach
    void setup() {
        tx.execute(status -> {
            Seller seller = Seller.create("concurrency-seller@t.com", "pw", "셀러", "010-0000-0000",
                    new Address("서울", "로1", "00000"), "컴퍼니", "대표", "000-00-00001");
            em.persist(seller);

            NormalMember member = NormalMember.create("concurrency@t.com", "pw", "회원", "닉",
                    LocalDate.of(1990, 1, 1), Gender.MALE, "010-0000-0001",
                    new Address("서울", "로2", "00000"));
            em.persist(member);

            Venue venue = Venue.create("정산홀", new Address("서울", "로3", "00000"), 1, 1);
            em.persist(venue);

            Event event = Event.create("정산공연", "설명", settlementDate.minusDays(3), settlementDate,
                    120, "출연", AgeLimit.ALL, Category.CONCERT, "url", seller);
            em.persist(event);

            EventSchedule schedule = EventSchedule.create(venue, settlementDate.atTime(19, 0), 1);
            event.addSchedule(schedule);
            em.persist(schedule);

            persistPaidReservation(member, schedule, "idem-concurrency-1");
            persistPaidReservation(member, schedule, "idem-concurrency-2");

            em.flush();
            return null;
        });
    }

    private void persistPaidReservation(NormalMember member, EventSchedule schedule, String idempotencyKey) {
        Reservation reservation = Reservation.create(member, schedule, idempotencyKey, PRICE);
        reservation.confirm();
        em.persist(reservation);
        em.persist(Payment.paid(reservation, PRICE));
    }

    @Test
    void 인스턴스가_여러_대여도_정산배치는_한_번만_실행된다() throws Exception {
        AtomicInteger attempts = new AtomicInteger();
        doAnswer(invocation -> {
            attempts.incrementAndGet();
            return invocation.callRealMethod();
        }).when(jobLauncher).run(any(Job.class), any(JobParameters.class));

        long lastIdBefore = lastJobExecutionId();

        ExecutorService es = Executors.newFixedThreadPool(INSTANCES);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(INSTANCES);

        for (int i = 0; i < INSTANCES; i++) {
            long offset = START_OFFSET_MS[i];
            es.submit(() -> {
                try {
                    startLatch.await();
                    Thread.sleep(offset);
                    settlementScheduler.runDaily();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        endLatch.await();
        es.shutdown();

        long executed = countJobExecutionsAfter(lastIdBefore);

        System.out.printf("%n========== [배치 실행] ==========%n" +
                        "  동시 실행 인스턴스 : %d대%n" +
                        "  배치 실행 시도     : %d회   (정상 1회)%n" +
                        "  배치 실행 완료     : %d회   (정상 1회)%n" +
                        "================================%n",
                INSTANCES, attempts.get(), executed);

        assertThat(attempts.get()).isEqualTo(1);
    }

    private long lastJobExecutionId() {
        return jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(job_execution_id), 0) FROM BATCH_JOB_EXECUTION", Long.class);
    }

    private long countJobExecutionsAfter(long afterId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BATCH_JOB_EXECUTION WHERE job_execution_id > ?", Long.class, afterId);
    }
}
