package com.ticketing.notification.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.notification.event.NotificationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRedisPublisher {

    public static final String NOTIFICATION_CREATED_CHANNEL =
            "notification.created";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(NotificationCreatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);

            Long subscriberCount = redisTemplate.convertAndSend(
                    NOTIFICATION_CREATED_CHANNEL,
                    message
            );

            log.debug(
                    "알림 실시간 신호 발행 완료: notificationId={}, memberId={}, subscriberCount={}",
                    event.notificationId(),
                    event.memberId(),
                    subscriberCount
            );
        } catch (JsonProcessingException | RuntimeException e) {
            log.error(
                    "알림 실시간 신호 발행 실패: notificationId={}, memberId={}",
                    event.notificationId(),
                    event.memberId(),
                    e
            );
        }
    }
}
