package com.ticketing.member.dto.response;

import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.MemberStatus;
import com.ticketing.member.domain.MemberType;
import com.ticketing.member.domain.Seller;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SellerResponseDto {

    private String email;

    private String name;

    private String phone;

    private MemberType memberType;

    private MemberStatus memberStatus;

    private Address address;

    private LocalDateTime createdAt;

    private String companyName;

    private String representativeName;

    private String businessNumber;


    public static SellerResponseDto from(Seller seller) {
        return SellerResponseDto.builder()
                .email(seller.getEmail())
                .name(seller.getName())
                .phone(seller.getPhone())
                .address(seller.getAddress())
                .createdAt(seller.getCreatedAt())
                .memberStatus(seller.getMemberStatus())
                .memberType(seller.getMemberType())
                .companyName(seller.getCompanyName())
                .representativeName(seller.getRepresentativeName())
                .businessNumber(seller.getBusinessNumber())
                .build();

    }
}
