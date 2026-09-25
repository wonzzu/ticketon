package com.ticketing.ticket.controller;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.ticket.dto.ElectronicTicketResponseDto;
import com.ticketing.ticket.service.ElectronicTicketQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "전자 티켓")
@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class ElectronicTicketController {

    private final ElectronicTicketQueryService electronicTicketQueryService;

    @Operation(summary = "내 예매의 전자 티켓 조회")
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<BaseResponse<List<ElectronicTicketResponseDto>>> findMine(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<ElectronicTicketResponseDto> tickets =
                electronicTicketQueryService.findMine(reservationId, user.getMemberId());

        return ResponseEntity.ok(BaseResponse.success(tickets));
    }
}
