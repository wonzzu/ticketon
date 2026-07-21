package com.ticketing.settlement.controller;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.settlement.dto.request.SettlementDetailSearchCond;
import com.ticketing.settlement.dto.response.SettlementDetailLineResponseDto;
import com.ticketing.settlement.dto.response.SettlementDetailSearchResponseDto;
import com.ticketing.settlement.dto.response.SettlementResponseDto;
import com.ticketing.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sellers/me")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/settlements")
    public ResponseEntity<BaseResponse<Page<SettlementResponseDto>>> findMySettlements(
            @AuthenticationPrincipal CustomUserDetails seller,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(BaseResponse.success(
                settlementService.findMySettlements(seller.getMemberId(), pageable)));
    }

    @GetMapping("/settlements/{settlementId}/details")
    public ResponseEntity<BaseResponse<Page<SettlementDetailLineResponseDto>>> findDetails(
            @PathVariable Long settlementId,
            @AuthenticationPrincipal CustomUserDetails seller,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(BaseResponse.success(
                settlementService.findMySettlementDetails(settlementId, seller.getMemberId(), pageable)));
    }

    @GetMapping("/settlement-details")
    public ResponseEntity<BaseResponse<Page<SettlementDetailSearchResponseDto>>> searchDetails(
            @RequestParam(required = false) Long paymentId,
            @RequestParam(required = false) Long reservationId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paidFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paidTo,
            @AuthenticationPrincipal CustomUserDetails seller,
            @PageableDefault(size = 20) Pageable pageable) {

        SettlementDetailSearchCond cond =
                new SettlementDetailSearchCond(paymentId, reservationId, eventId, paidFrom, paidTo);

        return ResponseEntity.ok(BaseResponse.success(
                settlementService.searchMyDetails(seller.getMemberId(), cond, pageable)));
    }
}
