package com.ticketing.member.repository;

import com.ticketing.member.domain.MemberHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberHistoryRepository extends JpaRepository<MemberHistory, Long> {

    List<MemberHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);

}
