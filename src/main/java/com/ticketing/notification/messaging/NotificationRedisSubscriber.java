package com.ticketing.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.notification.event.NotificationCreatedEvent;
import com.ticketing.notification.sse.NotificationSseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final NotificationSseEmitterRegistry emitterRegistry;

    @Override
    public void onMessage(
            Message message,
            byte[] pattern
    ) {
        try {
            NotificationCreatedEvent event = objectMapper.readValue(
                    message.getBody(),
                    NotificationCreatedEvent.class
            );

            emitterRegistry.send(
                    event.memberId(),
                    event
            );
        } catch (IOException e) {
            log.error(
                    "Redis 알림 메시지 역직렬화 실패: message={}",
                    new String(
                            message.getBody(),
                            StandardCharsets.UTF_8
                    ),
                    e
            );
        } catch (RuntimeException e) {
            log.error("Redis 알림 메시지 처리 실패", e);
        }
    }
}
