package com.ticketing.auth.service;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.auth.dto.request.LoginRequestDto;
import com.ticketing.auth.jwt.JwtTokenProvider;
import com.ticketing.auth.jwt.RefreshTokenStore;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Member;
import com.ticketing.member.domain.MemberStatus;
import com.ticketing.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ticketing.global.baseresponse.BaseResponseStatus.MEMBER_NOT_FOUND;
import static com.ticketing.global.baseresponse.BaseResponseStatus.UNAUTHORIZED_ACCESS;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenStore refreshTokenStore;


    public TokenPair login(LoginRequestDto dto) {

        Authentication authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        CustomUserDetails principal = (CustomUserDetails) authenticate.getPrincipal();

        Long memberId = principal.getMemberId();
        String role = principal.getMember().getMemberType().name();

        String accessToken = jwtTokenProvider.createAccessToken(memberId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId);

        refreshTokenStore.save(memberId, refreshToken);

        log.info("로그인 성공: memberId={}, role={}", memberId, role);

        return new TokenPair(accessToken, refreshToken);
    }

    public String reissue(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.validate(refreshToken)) {
            throw new BaseException(UNAUTHORIZED_ACCESS);
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);

        if (!refreshTokenStore.isValid(memberId, refreshToken)) {
            throw new BaseException(UNAUTHORIZED_ACCESS);
        }

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        MemberStatus status = member.getMemberStatus();
        if (status != MemberStatus.ACTIVE && status != MemberStatus.PENDING) {
            refreshTokenStore.delete(memberId);
            log.warn("정지/탈퇴 회원은 토큰 재발급 거부: memberId={},status={}", memberId, status);
            throw new BaseException(UNAUTHORIZED_ACCESS);
        }

        String role = member.getMemberType().name();
        return jwtTokenProvider.createAccessToken(memberId, role);
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.validate(refreshToken)) {
            return;
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        refreshTokenStore.delete(memberId);
        log.info("로그아웃: memberId={}", memberId);

    }

    public record TokenPair(String accessToken, String refreshToken) {
    }
}
