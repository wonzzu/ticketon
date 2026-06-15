package com.ticketing.member.repository;

import com.ticketing.member.domain.Member;
import com.ticketing.member.dto.request.MemberSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    Page<Member> search(MemberSearchCond cond, Pageable pageable);
}
