package com.ticketing.settlement.controller;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.settlement.batch.SettlementScheduler;
import com.ticketing.settlement.dto.response.SettlementDetailLineResponseDto;
import com.ticketing.settlement.dto.response.SettlementResponseDto;
import com.ticketing.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sellers/me/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<SettlementResponseDto>>> findMySettlements(
            @AuthenticationPrincipal CustomUserDetails seller,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(BaseResponse.success(
                settlementService.findMySettlements(seller.getMemberId(), pageable)));
    }

    @GetMapping("/{settlementId}/details")
    public ResponseEntity<BaseResponse<Page<SettlementDetailLineResponseDto>>> findDetails(
            @PathVariable Long settlementId,
            @AuthenticationPrincipal CustomUserDetails seller,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(BaseResponse.success(
                settlementService.findMySettlementDetails(settlementId, seller.getMemberId(), pageable)));
    }
}