package com.ticketing.funnel.service;

import com.ticketing.funnel.dto.response.ReservationFunnelResponseDto;
import com.ticketing.funnel.repository.ReservationFunnelProgressRepository;
import com.ticketing.funnel.repository.ReservationFunnelSummaryProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationFunnelQueryService {

    private final ReservationFunnelProgressRepository funnelRepository;

    @Value("${app.funnel.payment-timeout-minutes:7}")
    private long paymentTimeoutMinutes;

    public ReservationFunnelResponseDto findBySchedule(Long scheduleId) {
        LocalDateTime dropOffCutoff = LocalDateTime.now().minusMinutes(paymentTimeoutMinutes);
        ReservationFunnelSummaryProjection summary =
                funnelRepository.summarize(scheduleId, dropOffCutoff);
        return ReservationFunnelResponseDto.from(scheduleId, summary);
    }
}
