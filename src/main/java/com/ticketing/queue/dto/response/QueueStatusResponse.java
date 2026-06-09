package com.ticketing.queue.dto.response;


import com.ticketing.queue.domain.QueueStatus;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QueueStatusResponse {

    private QueueStatus status;
    private Long ahead;
    private Long total;

    public static QueueStatusResponse admitted() {
        return QueueStatusResponse.builder().status(QueueStatus.ADMITTED).build();
    }

    public static QueueStatusResponse waiting(Long ahead, Long total) {
        return QueueStatusResponse.builder()
                .status(QueueStatus.WAITING).ahead(ahead).total(total).build();
    }

    public static QueueStatusResponse expired() {
        return QueueStatusResponse.builder().status(QueueStatus.EXPIRED).build();
    }
}
