package com.ticketing.member.service;

import com.ticketing.global.exception.BaseException;
import com.ticketing.member.domain.Member;
import com.ticketing.member.domain.Seller;
import com.ticketing.member.dto.request.SellerSignupDto;
import com.ticketing.member.dto.request.SellerUpdateDto;
import com.ticketing.member.dto.response.SellerResponseDto;
import com.ticketing.member.repository.MemberRepository;
import com.ticketing.member.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ticketing.global.baseresponse.BaseResponseStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerService {

    private final MemberRepository memberRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void create(SellerSignupDto dto) {
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new BaseException(DUPLICATE_EMAIL);
        }
        Seller seller = Seller.create(
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getName(),
                dto.getPhone(),
                dto.getAddress(),
                dto.getCompanyName(),
                dto.getRepresentativeName(),
                dto.getBusinessNumber()
        );
        sellerRepository.save(seller);
        log.info("셀러 가입: sellerId={}, 회사={}", seller.getId(), dto.getCompanyName());
    }

    public SellerResponseDto findById(Long id) {
        return SellerResponseDto.from(
                sellerRepository.findById(id)
                        .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND))
        );
    }

    @Transactional
    public void update(Long id, SellerUpdateDto dto) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        seller.update(
                dto.getPhone(),
                dto.getAddress(),
                dto.getCompanyName(),
                dto.getRepresentativeName(),
                dto.getBusinessNumber()
        );
    }

    @Transactional
    public void delete(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        member.withdraw();
        log.info("셀러 탈퇴: memberId={}", id);
    }
}
