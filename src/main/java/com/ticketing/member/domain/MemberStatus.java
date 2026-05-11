package com.ticketing.member.domain;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {

    PENDING("이메일 인증 전"),
    ACTIVE("정상"),
    DORMANT("휴면"),
    SUSPENDED("정지"),
    WITHDRAWN("탈퇴");

    private final String description;
}
