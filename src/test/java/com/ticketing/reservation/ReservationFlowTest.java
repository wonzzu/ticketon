package com.ticketing.reservation;

import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventSchedule;
import com.ticketing.event.domain.EventSeat;
import com.ticketing.event.domain.EventSeatStatus;
import com.ticketing.event.repository.EventSeatRepository;
import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.Gender;
import com.ticketing.member.domain.NormalMember;
import com.ticketing.member.domain.Seller;
import com.ticketing.payment.domain.PaymentStatus;
import com.ticketing.payment.dto.request.PaymentCreateDto;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.payment.service.PaymentService;
import com.ticketing.reservation.domain.CancelReason;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.reservation.dto.request.ReservationCreateDto;
import com.ticketing.reservation.dto.response.ReservationResponseDto;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.service.ReservationService;
import com.ticketing.venue.domain.Seat;
import com.ticketing.venue.domain.SeatGrade;
import com.ticketing.venue.domain.Venue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 기능 테스트 (안전망) — 예약·결제·취소 happy path.
 * 성능 개선(인덱스·fetch join 등) 후에도 핵심 흐름이 깨지지 않는지 검증.
 * 취소 검증으로 예매↔결제 cancel 위임 리팩토링도 함께 보장.
 */
@SpringBootTest
@ActiveProfiles("test")   // 공통 테스트 설정 — ticketing_test DB + DataSeedRunner 비활성화
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)   // DB 정리
@DisplayName("기능 - 예약·결제·취소 happy path")
class ReservationFlowTest {

    @Autowired ReservationService reservationService;
    @Autowired PaymentService paymentService;
    @Autowired ReservationRepository reservationRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired EventSeatRepository eventSeatRepository;
    @Autowired StringRedisTemplate redis;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;
    @PersistenceContext EntityManager em;

    private Long memberId;
    private Long scheduleId;
    private Long eventSeatId;

    @BeforeEach
    void setup() {
        tx.execute(s -> {
            Seller seller = Seller.create("flow-seller@t.com", "pw", "셀러", "010-0000-0000",
                    new Address("서울", "로1", "00000"), "컴퍼니", "대표", "000-00-00001");
            em.persist(seller);

            NormalMember member = NormalMember.create("flow@t.com", "pw", "회원", "닉",
                    LocalDate.of(1990, 1, 1), Gender.MALE, "010-0000-0001", new Address("서울", "로2", "00000"));
            em.persist(member);

            Venue venue = Venue.create("플로우홀", new Address("서울", "로3", "00000"), 1, 1);
            em.persist(venue);

            Seat seat = Seat.of(venue, 1, 1, SeatGrade.VIP);
            em.persist(seat);

            Event event = Event.create("플로우공연", "설명", LocalDate.now(), LocalDate.now().plusDays(1),
                    120, "출연", AgeLimit.ALL, Category.CONCERT, "url", seller);
            event.approve();
            em.persist(event);

            EventSchedule schedule = EventSchedule.create(venue, LocalDateTime.now().plusDays(1), 1);
            event.addSchedule(schedule);
            em.persist(schedule);

            EventSeat eventSeat = EventSeat.create(schedule, seat, 10000);
            em.persist(eventSeat);

            em.flush();
            memberId = member.getId();
            scheduleId = schedule.getId();
            eventSeatId = eventSeat.getId();
            return null;
        });

        // 대기열 통과: active ZSet에 직접 등록(만료 10분 뒤) → queueService.isAdmitted = true
        redis.opsForZSet().add("queue:active:" + scheduleId, memberId.toString(),
                System.currentTimeMillis() + 600_000);
    }

    @AfterEach
    void cleanupRedis() {   // DB는 @Sql(truncate.sql)이 정리, Redis만 여기서
        redis.delete("queue:active:" + scheduleId);
        redis.delete("seat:hold:" + scheduleId + ":" + eventSeatId);
    }

    @Test
    @DisplayName("예약(PENDING) → 결제(CONFIRMED) → 취소(CANCEL + 결제취소)")
    void 예약_결제_취소() {
        // 1) 예약 생성 → PENDING
        ReservationCreateDto createDto = new ReservationCreateDto();
        ReflectionTestUtils.setField(createDto, "scheduleId", scheduleId);
        ReflectionTestUtils.setField(createDto, "eventSeatIds", List.of(eventSeatId));
        ReflectionTestUtils.setField(createDto, "idempotencyKey", "idem-flow-1");

        ReservationResponseDto created = reservationService.create(memberId, createDto);
        Long reservationId = created.getId();
        assertThat(created.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(created.getSeats()).hasSize(1);   // ReservationSeat 생성 (DTO seats라 LazyInit 안전)

        // 2) 결제 → 예약 CONFIRMED + 좌석 RESERVED + 선점 해제 + 금액
        PaymentCreateDto payDto = new PaymentCreateDto();
        ReflectionTestUtils.setField(payDto, "reservationId", reservationId);
        paymentService.pay(memberId, payDto);

        Reservation afterPay = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(afterPay.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(paymentRepository.existsByReservationId(reservationId)).isTrue();
        // 좌석 점유 (AVAILABLE → RESERVED)
        assertThat(eventSeatRepository.findById(eventSeatId).orElseThrow().getStatus())
                .isEqualTo(EventSeatStatus.RESERVED);
        // 결제 완료 시 Redis 선점 해제
        assertThat(redis.hasKey("seat:hold:" + scheduleId + ":" + eventSeatId)).isFalse();
        // 결제 금액 == 좌석 가격
        assertThat(paymentRepository.findByReservationId(reservationId).orElseThrow().getAmount())
                .isEqualTo(10000);

        // 3) 취소 → 예약 CANCEL + 결제 CANCELED + 좌석 복구 (cancel 위임 검증)
        reservationService.cancel(reservationId, memberId, CancelReason.CHANGE_OF_MIND, null);

        Reservation afterCancel = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(afterCancel.getStatus()).isEqualTo(ReservationStatus.CANCEL);
        assertThat(paymentRepository.findByReservationId(reservationId).orElseThrow().getStatus())
                .isEqualTo(PaymentStatus.CANCELED);
        // 좌석 복구 (RESERVED → AVAILABLE)
        assertThat(eventSeatRepository.findById(eventSeatId).orElseThrow().getStatus())
                .isEqualTo(EventSeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("같은 idempotencyKey로 두 번 예약 → 1건만 생성 (멱등성)")
    void 예약_멱등성() {
        ReservationCreateDto dto = new ReservationCreateDto();
        ReflectionTestUtils.setField(dto, "scheduleId", scheduleId);
        ReflectionTestUtils.setField(dto, "eventSeatIds", List.of(eventSeatId));
        ReflectionTestUtils.setField(dto, "idempotencyKey", "idem-dup");

        reservationService.create(memberId, dto);
        reservationService.create(memberId, dto);   // 같은 키 재시도 → 멱등 분기로 기존 반환

        assertThat(reservationRepository.count()).isEqualTo(1);   // 1건만 생성됨
    }
}
