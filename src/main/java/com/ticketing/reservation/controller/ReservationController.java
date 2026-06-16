package com.ticketing.reservation.controller;


import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.reservation.dto.request.ReservationCancelDto;
import com.ticketing.reservation.dto.request.ReservationCreateDto;
import com.ticketing.reservation.dto.request.ReservationSearchCond;
import com.ticketing.reservation.dto.response.ReservationResponseDto;
import com.ticketing.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<BaseResponse<ReservationResponseDto>> create(
            @Validated @RequestBody ReservationCreateDto dto, @AuthenticationPrincipal CustomUserDetails user) {
        ReservationResponseDto data = reservationService.create(user.getMemberId(), dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(data));
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<Page<ReservationResponseDto>>> findMine(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10) Pageable pageable) {

        ReservationSearchCond reservationSearchCond = new ReservationSearchCond(status, from, to);

        Page<ReservationResponseDto> mine = reservationService.findMine(user.getMemberId(), reservationSearchCond, pageable);

        return ResponseEntity.ok(BaseResponse.success(mine));

    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ReservationResponseDto>> findOne(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {

        ReservationResponseDto one = reservationService.findOne(id, user.getMemberId());

        return ResponseEntity.ok(BaseResponse.success(one));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BaseResponse<Void>> cancel(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user, @Validated @RequestBody ReservationCancelDto dto) {

        reservationService.cancel(id, user.getMemberId(), dto.getCancelReason(), dto.getDetail());

        return ResponseEntity.ok(BaseResponse.success());
    }

}
