package com.ticketing.support.reservation.controller;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.support.reservation.dto.RefundSnapshotResponseDto;
import com.ticketing.support.reservation.service.RefundSnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 고객지원")
@RestController
@RequiredArgsConstructor
@RequestMapping("/support/reservations")
public class SupportReservationController {

    private final RefundSnapshotService refundSnapshotService;

    @Operation(summary = "환불 예상 계산용 예매 스냅샷 조회")
    @GetMapping("/{reservationId}/refund-snapshot")
    public ResponseEntity<BaseResponse<RefundSnapshotResponseDto>> getRefundSnapshot(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user) {

        RefundSnapshotResponseDto snapshot =
                refundSnapshotService.getSnapshot(reservationId, user.getMemberId());

        return ResponseEntity.ok(BaseResponse.success(snapshot));
    }
}
