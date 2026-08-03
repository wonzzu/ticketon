package com.ticketing.venue.controller;

import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.venue.dto.request.VenueCreateDto;
import com.ticketing.venue.dto.request.VenueUpdateDto;
import com.ticketing.venue.dto.response.VenueResponseDto;
import com.ticketing.venue.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "공연장")
@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @Operation(summary = "공연장 등록")
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> create(@Validated @RequestBody VenueCreateDto dto) {
        venueService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success());
    }

    @Operation(summary = "공연장 목록")
    @GetMapping
    public ResponseEntity<BaseResponse<List<VenueResponseDto>>> findAll() {
        List<VenueResponseDto> venueList = venueService.findAll();
        return ResponseEntity.ok(BaseResponse.success(venueList));
    }

    @Operation(summary = "공연장 상세")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<VenueResponseDto>> getVenue(@PathVariable Long id) {
        VenueResponseDto data = venueService.findById(id);
        return ResponseEntity.ok(BaseResponse.success(data));
    }

    @Operation(summary = "공연장 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> updateVenue(@PathVariable Long id, @Validated @RequestBody VenueUpdateDto dto) {
        venueService.update(id, dto);
        return ResponseEntity.ok(BaseResponse.success());
    }

    @Operation(summary = "공연장 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteVenue(@PathVariable Long id) {
        venueService.delete(id);
        return ResponseEntity.ok(BaseResponse.success());
    }

}
