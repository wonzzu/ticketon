package com.ticketing.notification.dto.response;

import com.ticketing.notification.domain.Notification;
import org.springframework.data.domain.Slice;

import java.util.List;

public record NotificationSliceResponseDto(
        List<NotificationResponseDto> items,
        Long nextCursor,
        boolean hasNext
) {

    public static NotificationSliceResponseDto from(Slice<Notification> notificationSlice) {
        List<NotificationResponseDto> items = notificationSlice.getContent()
                .stream()
                .map(NotificationResponseDto::from)
                .toList();

        Long nextCursor = notificationSlice.hasNext() && !items.isEmpty()
                ? items.get(items.size() - 1).id()
                : null;

        return new NotificationSliceResponseDto(
                items,
                nextCursor,
                notificationSlice.hasNext()
        );
    }
}
