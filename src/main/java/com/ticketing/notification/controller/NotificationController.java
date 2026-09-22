package com.ticketing.notification.controller;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.notification.dto.response.NotificationSliceResponseDto;
import com.ticketing.notification.dto.response.ReadAllNotificationResponseDto;
import com.ticketing.notification.dto.response.UnreadNotificationCountResponseDto;
import com.ticketing.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "알림")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록 조회")
    @GetMapping
    public ResponseEntity<BaseResponse<NotificationSliceResponseDto>> findMine(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        NotificationSliceResponseDto data = notificationService.findMine(
                user.getMemberId(),
                cursor,
                size
        );

        return ResponseEntity.ok(BaseResponse.success(data));
    }

    @Operation(summary = "내 미읽음 알림 개수 조회")
    @GetMapping("/unread-count")
    public ResponseEntity<BaseResponse<UnreadNotificationCountResponseDto>> countUnread(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UnreadNotificationCountResponseDto data =
                notificationService.countUnread(user.getMemberId());

        return ResponseEntity.ok(BaseResponse.success(data));
    }

    @Operation(summary = "알림 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<BaseResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        notificationService.markAsRead(
                user.getMemberId(),
                notificationId
        );

        return ResponseEntity.ok(BaseResponse.success());
    }

    @Operation(summary = "모든 알림 읽음 처리")
    @PatchMapping("/read-all")
    public ResponseEntity<BaseResponse<ReadAllNotificationResponseDto>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        int updatedCount = notificationService.markAllAsRead(
                user.getMemberId()
        );

        ReadAllNotificationResponseDto data =
                ReadAllNotificationResponseDto.of(updatedCount);

        return ResponseEntity.ok(BaseResponse.success(data));
    }
}
