package com.ticketing.kafka.failure.controller;

import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.kafka.failure.domain.FailedKafkaEventStatus;
import com.ticketing.kafka.failure.dto.request.FailedKafkaEventSearchCondition;
import com.ticketing.kafka.failure.dto.response.FailedKafkaEventResponse;
import com.ticketing.kafka.failure.service.FailedKafkaEventService;
import com.ticketing.outbox.domain.OutboxConsumerType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 - Kafka 실패 이벤트")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/kafka-failures")
public class AdminFailedKafkaEventController {

    private final FailedKafkaEventService failedKafkaEventService;

    @Operation(summary = "Kafka 실패 이벤트 조회")
    @GetMapping
    public ResponseEntity<BaseResponse<Page<FailedKafkaEventResponse>>> search(
            @RequestParam(required = false) FailedKafkaEventStatus status,
            @RequestParam(required = false) OutboxConsumerType consumerType,
            @RequestParam(required = false) String originalTopic,
            @RequestParam(required = false) String eventId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        FailedKafkaEventSearchCondition condition = new FailedKafkaEventSearchCondition(
                status,
                consumerType,
                originalTopic,
                eventId
        );

        Page<FailedKafkaEventResponse> response = failedKafkaEventService.search(
                condition,
                pageable
        );

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "Kafka 실패 이벤트 재처리")
    @PostMapping("/{failedEventId}/reprocess")
    public ResponseEntity<BaseResponse<Void>> reprocess(
            @PathVariable Long failedEventId
    ) {
        failedKafkaEventService.reprocess(failedEventId);
        return ResponseEntity.ok(BaseResponse.success());
    }
}
