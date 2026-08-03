package com.ticketing.statistics.controller;

import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.statistics.dto.response.DailySalesStatsResponseDto;
import com.ticketing.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 - 통계")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/stats")
public class AdminStatsController {

    private final StatisticsService statisticService;

    @GetMapping("/daily")
    public ResponseEntity<BaseResponse<List<DailySalesStatsResponseDto>>> daily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate to) {

        List<DailySalesStatsResponseDto> range = statisticService.findRange(from, to);

        return ResponseEntity.ok(BaseResponse.success(range));
    }

    @PostMapping("/aggregate")
    public ResponseEntity<BaseResponse<Void>> aggregate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        statisticService.aggregateDaily(date);
        return ResponseEntity.ok(BaseResponse.success());
    }
}
