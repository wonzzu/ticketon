package com.ticketing.reservation;

import com.ticketing.event.domain.*;
import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.Gender;
import com.ticketing.member.domain.NormalMember;
import com.ticketing.member.domain.Seller;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("예매 - 멱등성 키 회원 소유 범위")
class ReservationIdempotencyOwnershipTest {

    @Autowired ReservationService reservationService;
    @Autowired ReservationRepository reservationRepository;
    @Autowired StringRedisTemplate redis;
    @Autowired TransactionTemplate tx;
    @PersistenceContext EntityManager em;

    private Long memberAId;
    private Long memberBId;
    private Long scheduleId;
    private Long eventSeatAId;
    private Long eventSeatBId;

    @BeforeEach
    void setup() {
        tx.executeWithoutResult(status -> {
            Seller seller = Seller.create("idempotency-seller@test.com", "pw", "판매자", "010-0000-0000",
                    new Address("서울", "테스트로 1", "00000"), "테스트컴퍼니", "대표자", "000-00-00001");
            em.persist(seller);

            NormalMember memberA = NormalMember.create("member-a@test.com", "pw", "회원A", "회원A닉네임",
                    LocalDate.of(1990, 1, 1), Gender.MALE, "010-0000-0001",
                    new Address("서울", "테스트로 2", "00000"));
            em.persist(memberA);

            NormalMember memberB = NormalMember.create("member-b@test.com", "pw", "회원B", "회원B닉네임",
                    LocalDate.of(1991, 1, 1), Gender.FEMALE, "010-0000-0002",
                    new Address("서울", "테스트로 3", "00000"));
            em.persist(memberB);

            Venue venue = Venue.create("멱등성 테스트 공연장",
                    new Address("서울", "테스트로 4", "00000"), 1, 2);
            em.persist(venue);

            Seat seatA = Seat.of(venue, 1, 1, SeatGrade.VIP);
            Seat seatB = Seat.of(venue, 1, 2, SeatGrade.VIP);
            em.persist(seatA);
            em.persist(seatB);

            Event event = Event.create("멱등성 테스트 공연", "설명", LocalDate.now(),
                    LocalDate.now().plusDays(1), 120, "출연자", AgeLimit.ALL,
                    Category.CONCERT, "poster-url", seller);
            event.approve();
            em.persist(event);

            EventSchedule schedule = EventSchedule.create(venue, LocalDateTime.now().plusDays(1), 2);
            event.addSchedule(schedule);
            em.persist(schedule);

            EventSeat eventSeatA = EventSeat.create(schedule, seatA, 10_000);
            EventSeat eventSeatB = EventSeat.create(schedule, seatB, 10_000);
            em.persist(eventSeatA);
            em.persist(eventSeatB);
            em.flush();

            memberAId = memberA.getId();
            memberBId = memberB.getId();
            scheduleId = schedule.getId();
            eventSeatAId = eventSeatA.getId();
            eventSeatBId = eventSeatB.getId();
        });

        long expireAt = System.currentTimeMillis() + 600_000;
        redis.opsForZSet().add("queue:active:" + scheduleId, memberAId.toString(), expireAt);
        redis.opsForZSet().add("queue:active:" + scheduleId, memberBId.toString(), expireAt);
    }

    @AfterEach
    void cleanupRedis() {
        redis.delete("queue:active:" + scheduleId);
        redis.delete("seat:hold:" + scheduleId + ":" + eventSeatAId);
        redis.delete("seat:hold:" + scheduleId + ":" + eventSeatBId);
    }

    @Test
    @DisplayName("서로 다른 회원은 같은 멱등성 키를 사용해도 각자의 예매를 생성한다")
    void 서로_다른_회원의_멱등성키는_별도_범위로_처리한다() {
        // given
        String sameIdempotencyKey = "same-key-for-different-members";

        ReservationCreateDto memberARequest = createRequest(eventSeatAId, sameIdempotencyKey);
        ReservationCreateDto memberBRequest = createRequest(eventSeatBId, sameIdempotencyKey);

        // when
        ReservationResponseDto reservationA = reservationService.create(memberAId, memberARequest);
        ReservationResponseDto reservationB = reservationService.create(memberBId, memberBRequest);

        // then
        assertThat(reservationB.getId()).isNotEqualTo(reservationA.getId());
        assertThat(reservationRepository.count()).isEqualTo(2);
    }

    private ReservationCreateDto createRequest(Long eventSeatId, String idempotencyKey) {
        ReservationCreateDto dto = new ReservationCreateDto();
        ReflectionTestUtils.setField(dto, "scheduleId", scheduleId);
        ReflectionTestUtils.setField(dto, "eventSeatIds", List.of(eventSeatId));
        ReflectionTestUtils.setField(dto, "idempotencyKey", idempotencyKey);
        return dto;
    }
}