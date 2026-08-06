package com.ticketing.payment.service;

import com.ticketing.event.domain.*;
import com.ticketing.event.repository.EventSeatRepository;
import com.ticketing.global.entity.Address;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Gender;
import com.ticketing.member.domain.NormalMember;
import com.ticketing.member.domain.Seller;
import com.ticketing.payment.dto.request.PaymentCreateDto;
import com.ticketing.payment.dto.response.PaymentResponseDto;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.reservation.dto.request.ReservationCreateDto;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.service.ReservationService;
import com.ticketing.venue.domain.Seat;
import com.ticketing.venue.domain.SeatGrade;
import com.ticketing.venue.domain.Venue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static com.ticketing.global.baseresponse.BaseResponseStatus.PAYMENT_ALREADY_COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("결제 - 동일 예약 동시 결제")
class PaymentConcurrencyTest {

    @Autowired PaymentService paymentService;
    @Autowired ReservationService reservationService;
    @Autowired PaymentRepository paymentRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired EventSeatRepository eventSeatRepository;
    @Autowired StringRedisTemplate redis;
    @Autowired TransactionTemplate tx;
    @PersistenceContext EntityManager em;

    private Long memberId;
    private Long scheduleId;
    private Long eventSeatId;
    private Long reservationId;

    @BeforeEach
    void setup() {
        tx.executeWithoutResult(status -> {
            Seller seller = Seller.create("payment-seller@test.com", "pw", "판매자", "010-0000-0000",
                    new Address("서울", "테스트로 1", "00000"), "테스트컴퍼니", "대표자", "000-00-00001");
            em.persist(seller);

            NormalMember member = NormalMember.create("payment-member@test.com", "pw", "회원", "회원 닉네임",
                    LocalDate.of(1990, 1, 1), Gender.MALE, "010-0000-0001",
                    new Address("서울", "테스트로 2", "00000"));
            em.persist(member);

            Venue venue = Venue.create("결제 테스트 공연장", new Address("서울", "테스트로 3", "00000"), 1, 1);
            em.persist(venue);

            Seat seat = Seat.of(venue, 1, 1, SeatGrade.VIP);
            em.persist(seat);

            Event event = Event.create("결제 테스트 공연", "동시 결제 테스트", LocalDate.now(),
                    LocalDate.now().plusDays(1), 120, "출연자", AgeLimit.ALL,
                    Category.CONCERT, "poster-url", seller);
            event.approve();
            em.persist(event);

            EventSchedule schedule = EventSchedule.create(venue, LocalDateTime.now().plusDays(1), 1);
            event.addSchedule(schedule);
            em.persist(schedule);

            EventSeat eventSeat = EventSeat.create(schedule, seat, 10_000);
            em.persist(eventSeat);
            em.flush();

            memberId = member.getId();
            scheduleId = schedule.getId();
            eventSeatId = eventSeat.getId();
        });

        redis.opsForZSet().add("queue:active:" + scheduleId, memberId.toString(),
                System.currentTimeMillis() + 600_000);

        ReservationCreateDto reservationRequest = new ReservationCreateDto();
        ReflectionTestUtils.setField(reservationRequest, "scheduleId", scheduleId);
        ReflectionTestUtils.setField(reservationRequest, "eventSeatIds", List.of(eventSeatId));
        ReflectionTestUtils.setField(reservationRequest, "idempotencyKey", "payment-concurrency-reservation");

        reservationId = reservationService.create(memberId, reservationRequest).getId();
    }

    @AfterEach
    void cleanupRedis() {
        redis.delete("queue:active:" + scheduleId);
        redis.delete("seat:hold:" + scheduleId + ":" + eventSeatId);
    }

    @Test
    @DisplayName("동일 예약의 동시 결제 요청은 한 건만 성공한다")
    void 동일_예약의_동시_결제는_한_건만_성공한다() throws Exception {
        PaymentCreateDto request = new PaymentCreateDto();
        ReflectionTestUtils.setField(request, "reservationId", reservationId);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        ConcurrentLinkedQueue<PaymentResponseDto> successes = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();

        Runnable paymentTask = () -> {
            readyLatch.countDown();

            try {
                startLatch.await();
                successes.add(paymentService.pay(memberId, request));
            } catch (Throwable e) {
                failures.add(e);
            }
        };

        Future<?> futureA = executor.submit(paymentTask);
        Future<?> futureB = executor.submit(paymentTask);

        try {
            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            futureA.get(10, TimeUnit.SECONDS);
            futureB.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        assertThat(successes).hasSize(1);
        assertThat(failures).hasSize(1);
        assertThat(failures.peek()).isInstanceOf(BaseException.class);

        BaseException failure = (BaseException) failures.peek();

        assertThat(failure.getBaseResponseStatus()).isEqualTo(PAYMENT_ALREADY_COMPLETED);
        assertThat(paymentRepository.count()).isEqualTo(1);
        assertThat(reservationRepository.findById(reservationId).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(eventSeatRepository.findById(eventSeatId).orElseThrow().getStatus())
                .isEqualTo(EventSeatStatus.RESERVED);
        assertThat(redis.hasKey("seat:hold:" + scheduleId + ":" + eventSeatId)).isFalse();
    }
}