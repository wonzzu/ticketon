package com.ticketing.admin.dto.response;

import com.ticketing.member.domain.Member;
import com.ticketing.member.domain.MemberHistory;
import com.ticketing.member.domain.MemberStatus;
import com.ticketing.member.domain.MemberType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdminMemberDetailResponseDto {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private MemberType memberType;
    private String memberTypeLabel;
    private MemberStatus memberStatus;
    private String memberStatusLabel;
    private LocalDateTime createdAt;
    private List<MemberHistoryResponseDto> histories;   // 같은 패키지라 import 불필요

    public static AdminMemberDetailResponseDto of(Member member, List<MemberHistory> histories) {
        return AdminMemberDetailResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .phone(member.getPhone())
                .memberType(member.getMemberType())
                .memberTypeLabel(member.getMemberType().getDescription())
                .memberStatus(member.getMemberStatus())
                .memberStatusLabel(member.getMemberStatus().getDescription())
                .createdAt(member.getCreatedAt())
                .histories(histories.stream().map(MemberHistoryResponseDto::from).toList())
                .build();
    }
}