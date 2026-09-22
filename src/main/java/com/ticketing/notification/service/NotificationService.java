package com.ticketing.notification.service;

import com.ticketing.global.exception.BaseException;
import com.ticketing.notification.domain.Notification;
import com.ticketing.notification.dto.response.NotificationSliceResponseDto;
import com.ticketing.notification.dto.response.UnreadNotificationCountResponseDto;
import com.ticketing.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.ticketing.global.baseresponse.BaseResponseStatus.INVALID_INPUT;
import static com.ticketing.global.baseresponse.BaseResponseStatus.NOTIFICATION_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationSliceResponseDto findMine(
            Long memberId,
            Long cursor,
            int size
    ) {
        validateSearchCondition(cursor, size);

        Pageable pageable = PageRequest.of(0, size);

        Slice<Notification> notifications;

        if (cursor == null) {
            notifications = notificationRepository
                    .findByMemberIdOrderByIdDesc(memberId, pageable);
        } else {
            notifications = notificationRepository
                    .findByMemberIdAndIdLessThanOrderByIdDesc(
                            memberId,
                            cursor,
                            pageable
                    );
        }

        return NotificationSliceResponseDto.from(notifications);
    }

    public UnreadNotificationCountResponseDto countUnread(Long memberId) {
        long count = notificationRepository
                .countByMemberIdAndReadAtIsNull(memberId);

        return UnreadNotificationCountResponseDto.of(count);
    }

    @Transactional
    public void markAsRead(Long memberId, Long notificationId) {
        Notification notification = notificationRepository
                .findByIdAndMemberId(notificationId, memberId)
                .orElseThrow(() -> new BaseException(NOTIFICATION_NOT_FOUND));

        notification.markAsRead(LocalDateTime.now());
    }

    @Transactional
    public int markAllAsRead(Long memberId) {
        return notificationRepository.markAllAsRead(
                memberId,
                LocalDateTime.now()
        );
    }

    private void validateSearchCondition(Long cursor, int size) {
        if (cursor != null && cursor <= 0) {
            throw new BaseException(INVALID_INPUT);
        }

        if (size < 1 || size > 50) {
            throw new BaseException(INVALID_INPUT);
        }
    }
}
