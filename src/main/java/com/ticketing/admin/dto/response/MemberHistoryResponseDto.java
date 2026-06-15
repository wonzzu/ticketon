package com.ticketing.admin.dto.response;


import com.ticketing.member.domain.MemberHistory;
import com.ticketing.member.domain.MemberStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberHistoryResponseDto {

    private MemberStatus previousStatus;
    private MemberStatus newStatus;
    private String reason;
    private String changedBy;
    private LocalDateTime changedAt;

    public static MemberHistoryResponseDto from(MemberHistory memberHistory) {
        return MemberHistoryResponseDto.builder()
                .previousStatus(memberHistory.getPreviousStatus())
                .newStatus(memberHistory.getNewStatus())
                .reason(memberHistory.getReason())
                .changedBy(memberHistory.getCreatedBy())
                .changedAt(memberHistory.getCreatedAt())
                .build();
    }
}
