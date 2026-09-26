package com.ticketing.funnel.domain;

import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "reservation_funnel_progress",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_funnel_journey", columnNames = "journey_id"),
                @UniqueConstraint(name = "uk_funnel_reservation", columnNames = "reservation_id")
        },
        indexes = @Index(name = "idx_funnel_schedule", columnList = "schedule_id")
)
public class ReservationFunnelProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "journey_id", length = 36)
    private String journeyId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "reservation_id")
    private Long reservationId;

    private LocalDateTime enteredAt;
    private LocalDateTime admittedAt;
    private LocalDateTime reservedAt;
    private LocalDateTime paidAt;
    private Long waitDurationMs;
    private Long paymentDurationMs;

    public static ReservationFunnelProgress start(String journeyId, Long memberId, Long scheduleId) {
        return ReservationFunnelProgress.builder()
                .journeyId(journeyId)
                .memberId(memberId)
                .scheduleId(scheduleId)
                .build();
    }

    public static ReservationFunnelProgress paymentFirst(
            Long reservationId,
            Long memberId,
            Long scheduleId,
            LocalDateTime paidAt
    ) {
        return ReservationFunnelProgress.builder()
                .reservationId(reservationId)
                .memberId(memberId)
                .scheduleId(scheduleId)
                .paidAt(paidAt)
                .build();
    }

    public void recordEntered(LocalDateTime enteredAt) {
        if (this.enteredAt == null || enteredAt.isBefore(this.enteredAt)) {
            this.enteredAt = enteredAt;
        }
        calculateWaitDuration();
    }

    public void recordAdmitted(LocalDateTime enteredAt, LocalDateTime admittedAt) {
        recordEntered(enteredAt);
        if (this.admittedAt == null || admittedAt.isBefore(this.admittedAt)) {
            this.admittedAt = admittedAt;
        }
        calculateWaitDuration();
    }

    public void recordReservation(
            String journeyId,
            Long reservationId,
            Long memberId,
            Long scheduleId,
            LocalDateTime reservedAt
    ) {
        validateIdentity(memberId, scheduleId);
        if (this.journeyId != null && !this.journeyId.equals(journeyId)) {
            throw new IllegalArgumentException("서로 다른 journeyId를 같은 퍼널에 연결할 수 없습니다.");
        }
        if (this.reservationId != null && !this.reservationId.equals(reservationId)) {
            throw new IllegalArgumentException("서로 다른 reservationId를 같은 퍼널에 연결할 수 없습니다.");
        }

        this.journeyId = journeyId;
        this.reservationId = reservationId;
        if (this.reservedAt == null || reservedAt.isBefore(this.reservedAt)) {
            this.reservedAt = reservedAt;
        }
        calculatePaymentDuration();
    }

    public void recordPaid(Long memberId, Long scheduleId, LocalDateTime paidAt) {
        validateIdentity(memberId, scheduleId);
        if (this.paidAt == null || paidAt.isBefore(this.paidAt)) {
            this.paidAt = paidAt;
        }
        calculatePaymentDuration();
    }

    public void mergePaymentFrom(ReservationFunnelProgress paymentFirstProgress) {
        if (paymentFirstProgress.paidAt != null) {
            recordPaid(
                    paymentFirstProgress.memberId,
                    paymentFirstProgress.scheduleId,
                    paymentFirstProgress.paidAt
            );
        }
    }

    private void calculateWaitDuration() {
        if (enteredAt == null || admittedAt == null) {
            return;
        }
        long duration = Duration.between(enteredAt, admittedAt).toMillis();
        if (duration < 0) {
            throw new IllegalArgumentException("입장 허용 시각은 대기열 진입 시각보다 빠를 수 없습니다.");
        }
        this.waitDurationMs = duration;
    }

    private void calculatePaymentDuration() {
        if (reservedAt == null || paidAt == null) {
            return;
        }
        long duration = Duration.between(reservedAt, paidAt).toMillis();
        if (duration < 0) {
            throw new IllegalArgumentException("결제 완료 시각은 예매 생성 시각보다 빠를 수 없습니다.");
        }
        this.paymentDurationMs = duration;
    }

    private void validateIdentity(Long memberId, Long scheduleId) {
        if (!this.memberId.equals(memberId) || !this.scheduleId.equals(scheduleId)) {
            throw new IllegalArgumentException("퍼널 이벤트의 회원 또는 공연 일정 정보가 일치하지 않습니다.");
        }
    }
}
