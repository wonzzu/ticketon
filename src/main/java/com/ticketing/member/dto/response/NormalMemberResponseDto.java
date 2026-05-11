package com.ticketing.member.dto.response;

import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.Gender;
import com.ticketing.member.domain.MemberStatus;
import com.ticketing.member.domain.MemberType;
import com.ticketing.member.domain.NormalMember;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NormalMemberResponseDto {

    private String email;

    private String name;

    private String phone;

    private String nickname;

    private LocalDate birthDate;

    private Gender gender;

    private Address address;

    private LocalDateTime createdAt;

    private MemberStatus memberStatus;

    private MemberType memberType;

    public static NormalMemberResponseDto from(NormalMember member) {
        return NormalMemberResponseDto.builder()
                .email(member.getEmail())
                .name(member.getName())
                .phone(member.getPhone())
                .nickname(member.getNickname())
                .birthDate(member.getBirthDate())
                .gender(member.getGender())
                .address(member.getAddress())
                .createdAt(member.getCreatedAt())
                .memberStatus(member.getMemberStatus())
                .memberType(member.getMemberType())
                .build();
    }

}
