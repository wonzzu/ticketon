package com.ticketing.member.domain;


import com.ticketing.global.entity.Address;
import com.ticketing.global.entity.BaseEntity;
import com.ticketing.global.exception.BaseException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static com.ticketing.global.baseresponse.BaseResponseStatus.INVALID_MEMBER_STATUS;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "member_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public abstract class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    protected String name;

    @Column(nullable = false)
    protected String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", insertable = false, updatable = false)
    private MemberType memberType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected MemberStatus memberStatus;

    @Embedded
    protected Address address;

    public void withdraw() {
        this.memberStatus = MemberStatus.WITHDRAWN;
    }

    public void suspend() {
        if (this.memberStatus == MemberStatus.WITHDRAWN) {
            throw new BaseException(INVALID_MEMBER_STATUS);
        }
        this.memberStatus = MemberStatus.SUSPENDED;
    }

    public void release() {
        if (this.memberStatus != MemberStatus.SUSPENDED) {
            throw new BaseException(INVALID_MEMBER_STATUS);
        }
        this.memberStatus = MemberStatus.ACTIVE;
    }
}
