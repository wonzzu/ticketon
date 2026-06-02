package com.ticketing.member.dto.response;

import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.Member;
import com.ticketing.member.domain.MemberType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MyInfoResponseDto {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private MemberType memberType;
    private String memberTypeLabel;
    private Address address;
    private LocalDateTime createdAt;

    public static MyInfoResponseDto from(Member member) {

        return MyInfoResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .phone(member.getPhone())
                .memberType(member.getMemberType())
                .memberTypeLabel(member.getMemberType().getDescription())
                .address(member.getAddress())
                .createdAt(member.getCreatedAt())
                .build();
    }

}
