package com.ticketing.event.controller;

import com.ticketing.event.dto.response.EventSeatResponseDto;
import com.ticketing.event.service.EventSeatService;
import com.ticketing.global.baseresponse.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules/{scheduleId}/seats")
public class EventSeatController {


    private final EventSeatService eventSeatService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<EventSeatResponseDto>>> findAll(@PathVariable Long scheduleId) {
        List<EventSeatResponseDto> seats = eventSeatService.findByScheduleId(scheduleId);
        return ResponseEntity.ok(BaseResponse.success(seats));
    }

}
