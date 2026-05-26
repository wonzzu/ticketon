package com.ticketing.admin.service;


import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventReviewHistory;
import com.ticketing.event.domain.EventStatus;
import com.ticketing.event.domain.ReviewAction;
import com.ticketing.event.dto.response.EventListResponseDto;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.event.repository.EventReviewHistoryRepository;
import com.ticketing.global.BaseResponseStatus;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.AdminMember;
import com.ticketing.member.repository.AdminMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ticketing.global.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEventService {

    private final EventRepository eventRepository;
    private final EventReviewHistoryRepository historyRepository;
    private final AdminMemberRepository adminMemberRepository;

    public List<EventListResponseDto> findPending() {
        return eventRepository.findByStatus(EventStatus.PENDING)
                .stream()
                .map(EventListResponseDto::from)
                .toList();
    }

    @Transactional
    public void approve(Long eventId, Long adminId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BaseException(PERFORMANCE_NOT_FOUND));

        AdminMember adminMember = adminMemberRepository.findById(adminId)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        EventStatus prev = event.getStatus();
        event.approve();

        historyRepository.save(
                EventReviewHistory.of(event, ReviewAction.APPROVED, prev, event.getStatus(), null, adminMember)
        );
    }

    @Transactional
    public void reject(Long eventId, Long adminId, String reason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BaseException(PERFORMANCE_NOT_FOUND));

        AdminMember adminMember = adminMemberRepository.findById(adminId)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        EventStatus prev = event.getStatus();
        event.reject();

        historyRepository.save(
                EventReviewHistory.of(event, ReviewAction.REJECTED, prev, event.getStatus(), reason, adminMember)
        );
    }

}
