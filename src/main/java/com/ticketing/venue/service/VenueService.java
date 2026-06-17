package com.ticketing.venue.service;

import com.ticketing.global.exception.BaseException;
import com.ticketing.venue.domain.SeatGradeRange;
import com.ticketing.venue.domain.Venue;
import com.ticketing.venue.dto.request.GradeRangeDto;
import com.ticketing.venue.dto.request.VenueCreateDto;
import com.ticketing.venue.dto.request.VenueUpdateDto;
import com.ticketing.venue.dto.response.VenueResponseDto;
import com.ticketing.venue.repository.SeatRepository;
import com.ticketing.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ticketing.global.baseresponse.BaseResponseStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public void save(VenueCreateDto dto) {
        Venue venue = Venue.create(dto.getName(), dto.getAddress(), dto.getRowCount(), dto.getColumnCount());
        venueRepository.save(venue);

        List<SeatGradeRange> ranges = dto.getRangeDtoList().stream()
                .map(GradeRangeDto::toGradeRange)
                .toList();
        seatRepository.saveAll(venue.assignSeats(dto.getRowCount(), dto.getColumnCount(), ranges));
        log.info("공연장 등록: venueId={}, name={}", venue.getId(), dto.getName());
    }

    public VenueResponseDto findById(Long id) {
        Venue venue = venueRepository.findById(id).orElseThrow(() -> new BaseException(VENUE_NOT_FOUND));
        return VenueResponseDto.from(venue);
    }

    @Transactional
    public void update(Long id, VenueUpdateDto dto) {
        Venue venue = venueRepository.findById(id).orElseThrow(() -> new BaseException(VENUE_NOT_FOUND));
        venue.update(dto.getName(), dto.getAddress());
    }

    @Transactional
    public void delete(Long id) {
        Venue venue = venueRepository.findById(id).orElseThrow(() -> new BaseException(VENUE_NOT_FOUND));
        venue.delete();

    }

    public List<VenueResponseDto> findAll() {
        return venueRepository.findAll()
                .stream()
                .map(VenueResponseDto::from)
                .toList();
    }
}
