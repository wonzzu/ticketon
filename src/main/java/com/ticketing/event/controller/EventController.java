package com.ticketing.event.controller;


import com.ticketing.event.dto.request.EventCreateDto;
import com.ticketing.event.dto.request.EventUpdateDto;
import com.ticketing.event.dto.response.EventListResponseDto;
import com.ticketing.event.dto.response.EventResponseDto;
import com.ticketing.event.service.EventService;
import com.ticketing.global.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;


    @PostMapping
    public ResponseEntity<BaseResponse<Void>> create(@Validated @RequestBody EventCreateDto dto) {
        eventService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success());
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<EventListResponseDto>>> findAll() {
        List<EventListResponseDto> eventList = eventService.findAll();
        return ResponseEntity.ok(BaseResponse.success(eventList));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<EventResponseDto>> findById(@PathVariable Long id) {
        EventResponseDto event = eventService.find(id);
        return ResponseEntity.ok(BaseResponse.success(event));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> update(@PathVariable Long id, @Validated @RequestBody EventUpdateDto dto) {
        eventService.update(id, dto);
        return ResponseEntity.ok(BaseResponse.success());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.ok(BaseResponse.success());
    }

}
