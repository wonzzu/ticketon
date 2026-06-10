package com.ticketing.coupon.repository;

import com.ticketing.coupon.domain.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {

    List<CouponIssue> findByMemberId(Long memberId);
}
