package com.ticketing.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberType {

    NORMAL("일반 회원"),
    SELLER("판매자"),
    ADMIN("관리자");

    private final String description;
}
