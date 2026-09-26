package com.ticketing.funnel.controller;

import com.ticketing.funnel.dto.response.ReservationFunnelResponseDto;
import com.ticketing.funnel.service.ReservationFunnelQueryService;
import com.ticketing.global.baseresponse.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 - 예매 전환 분석")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/funnels")
public class AdminFunnelController {

    private final ReservationFunnelQueryService funnelQueryService;

    @Operation(summary = "공연 일정별 예매 전환 퍼널 조회")
    @GetMapping("/schedules/{scheduleId}")
    public ResponseEntity<BaseResponse<ReservationFunnelResponseDto>> findBySchedule(
            @PathVariable Long scheduleId
    ) {
        ReservationFunnelResponseDto response = funnelQueryService.findBySchedule(scheduleId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
