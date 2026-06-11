package com.ticketing.member.service;

import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Member;
import com.ticketing.member.dto.response.MyInfoResponseDto;
import com.ticketing.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ticketing.global.baseresponse.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MyInfoResponseDto getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        return MyInfoResponseDto.from(member);
    }
}
