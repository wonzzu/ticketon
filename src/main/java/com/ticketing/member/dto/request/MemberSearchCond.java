package com.ticketing.member.dto.request;

import com.ticketing.member.domain.MemberStatus;
import com.ticketing.member.domain.MemberType;

public record MemberSearchCond(String email, String name, MemberStatus memberStatus, MemberType memberType) {

}
