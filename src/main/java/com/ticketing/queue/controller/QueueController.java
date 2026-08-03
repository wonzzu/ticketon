package com.ticketing.queue.controller;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.queue.dto.response.QueueStatusResponse;
import com.ticketing.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "대기열")
@RestController
@RequiredArgsConstructor
@RequestMapping("/queue")
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/{scheduleId}/enter")
    public ResponseEntity<BaseResponse<QueueStatusResponse>> enter(
            @PathVariable Long scheduleId, @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(BaseResponse.success(queueService.enter(scheduleId, user.getMemberId())));
    }

    @GetMapping("/{scheduleId}/status")
    public ResponseEntity<BaseResponse<QueueStatusResponse>> status(
            @PathVariable Long scheduleId, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(BaseResponse.success(queueService.status(scheduleId, user.getMemberId())));
    }
}
