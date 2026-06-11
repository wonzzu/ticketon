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

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<BaseResponse<Void>> create(@Validated @RequestBody VenueCreateDto dto) {
        venueService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success());
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<VenueResponseDto>>> findAll() {
        List<VenueResponseDto> venueList = venueService.findAll();
        return ResponseEntity.ok(BaseResponse.success(venueList));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<VenueResponseDto>> getVenue(@PathVariable Long id) {
        VenueResponseDto data = venueService.findById(id);
        return ResponseEntity.ok(BaseResponse.success(data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> updateVenue(@PathVariable Long id, @Validated @RequestBody VenueUpdateDto dto) {
        venueService.update(id, dto);
        return ResponseEntity.ok(BaseResponse.success());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteVenue(@PathVariable Long id) {
        venueService.delete(id);
        return ResponseEntity.ok(BaseResponse.success());
    }

}
