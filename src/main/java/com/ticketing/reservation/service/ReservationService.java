package com.ticketing.reservation.service;

import com.ticketing.event.domain.EventSchedule;
import com.ticketing.event.domain.EventSeat;
import com.ticketing.event.domain.EventSeatStatus;
import com.ticketing.event.repository.EventScheduleRepository;
import com.ticketing.event.repository.EventSeatRepository;
import com.ticketing.event.service.SeatHoldService;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Member;
import com.ticketing.member.repository.MemberRepository;
import com.ticketing.payment.domain.PaymentHistory;
import com.ticketing.payment.repository.PaymentHistoryRepository;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.queue.service.QueueService;
import com.ticketing.reservation.domain.CancelReason;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationHistory;
import com.ticketing.reservation.domain.ReservationSeat;
import com.ticketing.reservation.dto.request.ReservationCreateDto;
import com.ticketing.reservation.dto.response.ReservationResponseDto;
import com.ticketing.reservation.repository.ReservationHistoryRepository;
import com.ticketing.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.ticketing.global.baseresponse.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private static final int MAX_SEATS = 3;

    private final ReservationRepository reservationRepository;
    private final EventScheduleRepository eventScheduleRepository;
    private final EventSeatRepository eventSeatRepository;
    private final MemberRepository memberRepository;
    private final SeatHoldService seatHoldService;
    private final QueueService queueService;
    private final ReservationHistoryRepository reservationHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;


    @Transactional
    public ReservationResponseDto create(Long memberId, ReservationCreateDto dto) {
        Optional<Reservation> exist = reservationRepository.findByIdempotencyKey(dto.getIdempotencyKey());

        if (exist.isPresent()) {
            return ReservationResponseDto.from(exist.get());
        }

        // 대기열 입장(승급) 안 했으면 예매 불가
        if (!queueService.isAdmitted(dto.getScheduleId(), memberId)) {
            throw new BaseException(QUEUE_NOT_ADMITTED);
        }

        List<Long> seatIds = dto.getEventSeatIds();

        if (seatIds.isEmpty()) {
            throw new BaseException(EMPTY_SEAT_SELECTION);
        }
        if (seatIds.size() > MAX_SEATS) {
            throw new BaseException(EXCEED_SEAT_LIMIT);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        EventSchedule schedule = eventScheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new BaseException(PERFORMANCE_NOT_FOUND));

        List<EventSeat> seats = eventSeatRepository.findAllById(seatIds);

        if (seats.size() != seatIds.size()) {
            throw new BaseException(SEAT_NOT_AVAILABLE);
        }

        boolean anySold = seats.stream().anyMatch(s -> s.getStatus() == EventSeatStatus.RESERVED);
        if (anySold) {
            throw new BaseException(SEAT_NOT_AVAILABLE);
        }

        if (!seatHoldService.holdAll(dto.getScheduleId(), seatIds, memberId)) {
            throw new BaseException(SEAT_NOT_AVAILABLE);
        }

        int totalPrice = seats.stream().mapToInt(EventSeat::getPrice).sum();

        Reservation reservation = Reservation.create(member, schedule, dto.getIdempotencyKey(), totalPrice);

        for (EventSeat seat : seats) {
            reservation.addReservationSeat(ReservationSeat.create(seat, seat.getPrice()));
        }
        reservationRepository.save(reservation);

        reservationHistoryRepository.save(ReservationHistory.of(reservation));

        return ReservationResponseDto.from(reservation);
    }

    // TODO: findMine은 예매 N건마다 schedule/event/venue/seat을 LAZY 조회 → N+1 나중에 fetch join 생각.
    public List<ReservationResponseDto> findMine(Long memberId) {
        return reservationRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream().map(ReservationResponseDto::from)
                .toList();
    }

    public ReservationResponseDto findOne(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BaseException(RESERVATION_NOT_FOUND));

        if (!reservation.isOwnedBy(memberId)) {
            throw new BaseException(RESERVATION_NOT_OWNED);
        }

        return ReservationResponseDto.from(reservation);
    }

    @Transactional
    public void cancel(Long reservationId, Long memberId, CancelReason cancelReason,String detail) {

        if (cancelReason == CancelReason.OTHER && (detail == null || detail.isBlank())) {
            throw new BaseException(CANCEL_DETAIL_REQUIRED);
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BaseException(RESERVATION_NOT_FOUND));

        if (!reservation.isOwnedBy(memberId)) {
            throw new BaseException(RESERVATION_NOT_OWNED);
        }

        reservation.cancel();
        reservation.getReservationSeats().forEach(rs -> rs.getEventSeat().cancel());

        Long scheduleId = reservation.getEventSchedule().getId();
        List<Long> seatIds = reservation.getReservationSeats().stream()
                .map(rs -> rs.getEventSeat().getId()).toList();

        seatHoldService.releaseAll(scheduleId, seatIds);

        paymentRepository.findByReservationId(reservationId).ifPresent(payment -> {
            payment.cancel();
            paymentHistoryRepository.save(PaymentHistory.of(payment, cancelReason.getDescription()));
        });

        reservationHistoryRepository.save(ReservationHistory.ofCancel(reservation,cancelReason,detail));
    }
}
