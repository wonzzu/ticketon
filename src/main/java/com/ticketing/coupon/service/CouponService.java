package com.ticketing.coupon.service;

import com.ticketing.coupon.domain.Coupon;
import com.ticketing.coupon.domain.CouponIssue;
import com.ticketing.coupon.dto.request.CouponCreateDto;
import com.ticketing.coupon.repository.CouponIssueRepository;
import com.ticketing.coupon.repository.CouponRepository;
import com.ticketing.global.BaseResponseStatus;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Member;
import com.ticketing.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ticketing.global.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final StringRedisTemplate redisTemplate;
    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createCoupon(CouponCreateDto dto) {

        Coupon coupon = couponRepository.save(
                Coupon.create(
                        dto.getName(), dto.getDiscountType(),
                        dto.getDiscountValue(), dto.getTotalQuantity()));

        redisTemplate.opsForValue().set(stockKey(coupon.getId()), String.valueOf(dto.getTotalQuantity()));

        return coupon.getId();
    }

    @Transactional
    public void issue(Long couponId, Long memberId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BaseException(COUPON_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        String issuedKey = issuedKey(couponId);
        String stockKey = stockKey(couponId);
        String me = memberId.toString();

        Long added = redisTemplate.opsForSet().add(issuedKey, me);
        if (added == null || added == 0) {
            throw new BaseException(COUPON_ALREADY_ISSUED);
        }

        Long remaining = redisTemplate.opsForValue().decrement(stockKey);
        if (remaining == null || remaining < 0) {
            redisTemplate.opsForValue().increment(stockKey);
            redisTemplate.opsForSet().remove(issuedKey, me);
            throw new BaseException(COUPON_SOLD_OUT);
        }

        try {
            couponIssueRepository.save(CouponIssue.create(coupon, member));
        } catch (RuntimeException e) {
            redisTemplate.opsForValue().increment(stockKey);
            redisTemplate.opsForSet().remove(issuedKey, me);
            throw e;
        }

    }


    private String stockKey(Long id) {
        return "coupon:stock:" + id;
    }

    private String issuedKey(Long id) {
        return "coupon:issued:" + id;
    }
}
