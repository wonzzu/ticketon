package com.ticketing.event.controller;


import com.ticketing.auth.CustomUserDetails;
import com.ticketing.event.domain.Category;
import com.ticketing.event.dto.request.EventCreateDto;
import com.ticketing.event.dto.request.EventUpdateDto;
import com.ticketing.event.dto.response.EventListResponseDto;
import com.ticketing.event.dto.response.EventResponseDto;
import com.ticketing.event.service.EventService;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final StatisticsService statisticsService;


    @PostMapping
    public ResponseEntity<BaseResponse<Void>> create(@Validated @RequestBody EventCreateDto dto,
                                                     @AuthenticationPrincipal CustomUserDetails seller) {
        eventService.save(dto, seller.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success());
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<EventListResponseDto>>> findAll(
            @RequestParam(required = false) Category category,
            @RequestParam(name = "q", required = false) String keyword) {

        List<EventListResponseDto> eventList = eventService.search(category, keyword);
        return ResponseEntity.ok(BaseResponse.success(eventList));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<EventResponseDto>> findById(@PathVariable Long id) {
        EventResponseDto event = eventService.find(id);
        return ResponseEntity.ok(BaseResponse.success(event));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> update(@PathVariable Long id,
                                                     @Validated @RequestBody EventUpdateDto dto,
                                                     @AuthenticationPrincipal CustomUserDetails seller) {
        eventService.update(id, dto, seller.getMemberId());
        return ResponseEntity.ok(BaseResponse.success());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id,
                                                     @AuthenticationPrincipal CustomUserDetails seller) {
        eventService.delete(id, seller.getMemberId());

        return ResponseEntity.ok(BaseResponse.success());
    }

    @GetMapping("/ranking")
    public ResponseEntity<BaseResponse<List<EventListResponseDto>>> ranking(
            @RequestParam(defaultValue = "popular") String sort,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {

        List<EventListResponseDto> ranking = statisticsService.getRanking(sort,days, limit);

        return ResponseEntity.ok(BaseResponse.success(ranking));
    }
}
