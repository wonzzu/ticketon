package com.ticketing.admin.controller;

import com.ticketing.admin.dto.request.EventRejectDto;
import com.ticketing.admin.service.AdminEventService;
import com.ticketing.auth.CustomUserDetails;
import com.ticketing.event.dto.response.EventListResponseDto;
import com.ticketing.event.dto.response.EventResponseDto;
import com.ticketing.global.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {

    private final AdminEventService adminEventService;

    @GetMapping("/pending")
    public ResponseEntity<BaseResponse<List<EventListResponseDto>>> findPending() {
        List<EventListResponseDto> pending = adminEventService.findPending();

        return ResponseEntity.ok(BaseResponse.success(pending));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<BaseResponse<Void>> approve(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails admin) {
        adminEventService.approve(id, admin.getMemberId());

        return ResponseEntity.ok(BaseResponse.success());
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<BaseResponse<Void>> reject(@PathVariable Long id,
                                                     @Validated @RequestBody EventRejectDto dto,
                                                     @AuthenticationPrincipal CustomUserDetails admin) {
        adminEventService.reject(id, admin.getMemberId(),dto.getReason());

        return ResponseEntity.ok(BaseResponse.success());
    }

}
