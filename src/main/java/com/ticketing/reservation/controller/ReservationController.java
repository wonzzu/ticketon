package com.ticketing.reservation.controller;


import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.BaseResponse;
import com.ticketing.reservation.dto.request.ReservationCreateDto;
import com.ticketing.reservation.dto.response.ReservationResponseDto;
import com.ticketing.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<BaseResponse<List<ReservationResponseDto>>> findMine(@AuthenticationPrincipal CustomUserDetails user) {
        List<ReservationResponseDto> mine = reservationService.findMine(user.getMemberId());

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
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {
        reservationService.cancel(id, user.getMemberId());
        return ResponseEntity.ok(BaseResponse.success());
    }

}
