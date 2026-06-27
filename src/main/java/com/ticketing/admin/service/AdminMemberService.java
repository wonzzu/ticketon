package com.ticketing.admin.service;

import com.ticketing.admin.dto.response.AdminMemberDetailResponseDto;
import com.ticketing.admin.dto.response.AdminMemberListResponseDto;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Member;
import com.ticketing.member.domain.MemberHistory;
import com.ticketing.member.domain.MemberStatus;
import com.ticketing.member.dto.request.MemberSearchCond;
import com.ticketing.member.repository.MemberHistoryRepository;
import com.ticketing.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ticketing.global.baseresponse.BaseResponseStatus.MEMBER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;
    private final MemberHistoryRepository memberHistoryRepository;

    public Page<AdminMemberListResponseDto> search(MemberSearchCond cond, Pageable pageable) {
        return memberRepository.search(cond, pageable)
                .map(AdminMemberListResponseDto::from);
    }

    public AdminMemberDetailResponseDto findDetail(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        List<MemberHistory> historyList = memberHistoryRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

        return AdminMemberDetailResponseDto.of(member, historyList);
    }

    @Transactional
    public void suspend(Long memberId, String reason) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        MemberStatus prev = member.getMemberStatus();

        member.suspend();
        memberHistoryRepository.save(MemberHistory.of(member.getId(), prev, member.getMemberStatus(), reason));
        log.info("회원 정지: memberId={}, {}->{},사유={}", memberId, prev, member.getMemberStatus(), reason);
    }

    @Transactional
    public void release(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        MemberStatus prev = member.getMemberStatus();

        member.release();

        memberHistoryRepository.save(MemberHistory.of(member.getId(), prev, member.getMemberStatus(), null));
        log.info("회원 정지 해제: memberId={}, {}->{}", memberId, prev, member.getMemberStatus());
    }
}