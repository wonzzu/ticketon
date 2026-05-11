package com.ticketing.event.controller;

import com.ticketing.event.dto.request.EventScheduleCreateDto;
import com.ticketing.event.dto.response.EventScheduleResponseDto;
import com.ticketing.event.service.EventScheduleService;
import com.ticketing.global.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/schedules")
public class EventScheduleController {

    private final EventScheduleService eventScheduleService;

    @PostMapping
    public ResponseEntity<BaseResponse<Void>> create(@PathVariable Long eventId,
                                                     @Validated @RequestBody EventScheduleCreateDto dto) {
        eventScheduleService.create(eventId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success());
    }


    @GetMapping
    public ResponseEntity<BaseResponse<List<EventScheduleResponseDto>>> findAll(@PathVariable Long eventId) {
        List<EventScheduleResponseDto> events = eventScheduleService.findByEvent(eventId);
        return ResponseEntity.ok(BaseResponse.success(events));
    }
}
