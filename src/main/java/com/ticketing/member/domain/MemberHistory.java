package com.ticketing.member.domain;

import com.ticketing.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class MemberHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus newStatus;

    @Column(length = 500)
    private String reason;


    public static MemberHistory of(Long memberId, MemberStatus previousStatus, MemberStatus newStatus, String reason) {
        return MemberHistory.builder()
                .memberId(memberId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(reason)
                .build();
    }


}
