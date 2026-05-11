package com.ticketing.member.repository;

import com.ticketing.member.domain.NormalMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NormalMemberRepository extends JpaRepository<NormalMember, Long> {

    boolean existsByNickname(String nickname);
}
