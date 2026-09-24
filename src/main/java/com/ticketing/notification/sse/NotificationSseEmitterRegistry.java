package com.ticketing.notification.sse;

import com.ticketing.notification.event.NotificationCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotificationSseEmitterRegistry {

    private static final long SSE_TIMEOUT_MILLIS = 60 * 60 * 1000L;

    private final Map<Long, Set<SseEmitter>> emitters =
            new ConcurrentHashMap<>();

    public SseEmitter connect(Long memberId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);

        emitters.computeIfAbsent(
                memberId,
                ignored -> ConcurrentHashMap.newKeySet()
        ).add(emitter);

        emitter.onCompletion(() -> remove(memberId, emitter));
        emitter.onTimeout(() -> remove(memberId, emitter));
        emitter.onError(error -> remove(memberId, emitter));

        sendConnectedEvent(memberId, emitter);

        return emitter;
    }

    public void send(
            Long memberId,
            NotificationCreatedEvent event
    ) {
        Set<SseEmitter> memberEmitters = emitters.get(memberId);

        if (memberEmitters == null || memberEmitters.isEmpty()) {
            return;
        }

        memberEmitters.forEach(emitter ->
                sendNotification(memberId, emitter, event)
        );
    }

    public void sendHeartbeat() {
        emitters.forEach((memberId, memberEmitters) ->
                memberEmitters.forEach(emitter ->
                        sendHeartbeat(memberId, emitter)
                )
        );
    }

    private void sendConnectedEvent(
            Long memberId,
            SseEmitter emitter
    ) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connected")
                            .data("SSE 연결이 완료되었습니다.")
            );
        } catch (IOException | IllegalStateException e) {
            remove(memberId, emitter);
            emitter.completeWithError(e);
        }
    }

    private void sendNotification(
            Long memberId,
            SseEmitter emitter,
            NotificationCreatedEvent event
    ) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .id(event.notificationId().toString())
                            .name("notification")
                            .data(event)
            );
        } catch (IOException | IllegalStateException e) {
            remove(memberId, emitter);
            emitter.completeWithError(e);

            log.debug(
                    "종료된 SSE 연결 제거: memberId={}, notificationId={}",
                    memberId,
                    event.notificationId()
            );
        }
    }

    private void sendHeartbeat(
            Long memberId,
            SseEmitter emitter
    ) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .name("heartbeat")
                            .data("keep-alive")
            );
        } catch (IOException | IllegalStateException e) {
            remove(memberId, emitter);
            emitter.completeWithError(e);

            log.debug(
                    "Heartbeat 실패로 SSE 연결 제거: memberId={}",
                    memberId
            );
        }
    }

    private void remove(
            Long memberId,
            SseEmitter emitter
    ) {
        Set<SseEmitter> memberEmitters = emitters.get(memberId);

        if (memberEmitters == null) {
            return;
        }

        memberEmitters.remove(emitter);

        if (memberEmitters.isEmpty()) {
            emitters.remove(memberId, memberEmitters);
        }
    }
}
