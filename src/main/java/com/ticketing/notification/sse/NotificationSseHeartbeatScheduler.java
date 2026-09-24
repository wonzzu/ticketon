package com.ticketing.notification.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationSseHeartbeatScheduler {

    private final NotificationSseEmitterRegistry emitterRegistry;

    @Scheduled(
            fixedDelayString =
                    "${notification.sse.heartbeat-interval-ms:30000}"
    )
    public void sendHeartbeat() {
        emitterRegistry.sendHeartbeat();
    }
}
