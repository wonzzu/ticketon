package com.ticketing.member.service;

import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Member;
import com.ticketing.member.domain.NormalMember;
import com.ticketing.member.dto.request.MemberUpdateDto;
import com.ticketing.member.dto.request.NormalMemberSignupDto;
import com.ticketing.member.dto.response.NormalMemberResponseDto;
import com.ticketing.member.repository.MemberRepository;
import com.ticketing.member.repository.NormalMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ticketing.global.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NormalMemberService {

    private final MemberRepository memberRepository;
    private final NormalMemberRepository normalMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void create(NormalMemberSignupDto dto) {
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new BaseException(DUPLICATE_EMAIL);
        }
        if (normalMemberRepository.existsByNickname(dto.getNickname())) {
            throw new BaseException(DUPLICATE_NICKNAME);
        }
        NormalMember normalMember = NormalMember.create(
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getName(),
                dto.getNickname(),
                dto.getBirthDate(),
                dto.getGender(),
                dto.getPhone(),
                dto.getAddress()
        );
        normalMemberRepository.save(normalMember);
    }

    public NormalMemberResponseDto findById(Long id) {
        return NormalMemberResponseDto.from(
                normalMemberRepository.findById(id)
                        .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND))
        );
    }

    @Transactional
    public void update(Long id, MemberUpdateDto dto) {
        NormalMember member = normalMemberRepository.findById(id)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        member.changeMember(dto.getNickName(), dto.getPhone(), dto.getAddress());
    }

    @Transactional
    public void delete(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        member.withdraw();
    }
}
