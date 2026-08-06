package com.ticketing.coupon.service;

import com.ticketing.coupon.domain.Coupon;
import com.ticketing.coupon.domain.CouponIssue;
import com.ticketing.coupon.dto.request.CouponCreateDto;
import com.ticketing.coupon.dto.response.CouponResponseDto;
import com.ticketing.coupon.dto.response.MyCouponResponseDto;
import com.ticketing.coupon.repository.CouponIssueRepository;
import com.ticketing.coupon.repository.CouponRepository;
import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Member;
import com.ticketing.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static com.ticketing.global.baseresponse.BaseResponseStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final StringRedisTemplate redisTemplate;
    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final MemberRepository memberRepository;
    private final RedisScript<Long> couponIssueScript;
    private final RedisScript<Long> couponRestoreScript;

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

        Long result = redisTemplate.execute(
                couponIssueScript,
                List.of(issuedKey, stockKey),
                me
        );

        if (result == -1L) {
            throw new BaseException(COUPON_ALREADY_ISSUED);
        }
        if (result == -2L) {
            log.warn("쿠폰 소진: couponId={}, memberId={}", couponId, memberId);
            throw new BaseException(COUPON_SOLD_OUT);
        }

        registerRollbackCompensation(issuedKey, stockKey, me);

        couponIssueRepository.save(CouponIssue.create(coupon, member));
        log.info("쿠폰 발급 완료: couponId={}, memberId={}, 잔여재고={}", couponId, memberId, result);
    }


    public List<CouponResponseDto> findAll() {
        return couponRepository.findAll().stream()
                .map(CouponResponseDto::from)
                .toList();
    }

    public List<MyCouponResponseDto> findMyCoupon(Long memberId) {
        return couponIssueRepository.findByMemberId(memberId).stream()
                .map(MyCouponResponseDto::from)
                .toList();
    }


    private void registerRollbackCompensation(String issuedKey, String stockKey, String memberId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_ROLLED_BACK) return;

                try {
                    Long restored = redisTemplate.execute(
                            couponRestoreScript,
                            List.of(issuedKey, stockKey),
                            memberId
                    );

                    if (Long.valueOf(1L).equals(restored)) {
                        log.warn("쿠폰 발급 롤백 보상 완료: issuedKey={}, memberId={}", issuedKey, memberId);
                    }
                } catch (RuntimeException e) {
                    log.error("쿠폰 발급 롤백 보상 실패: issuedKey={}, memberId={}", issuedKey, memberId, e);
                }
            }
        });
    }


    private String stockKey(Long id) {
        return "coupon:stock:" + id;
    }

    private String issuedKey(Long id) {
        return "coupon:issued:" + id;
    }
}
