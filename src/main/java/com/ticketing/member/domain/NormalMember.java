package com.ticketing.member.domain;


import com.ticketing.global.entity.Address;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@DiscriminatorValue("NORMAL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class NormalMember extends Member {

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;


    public static NormalMember create(String email, String encodedPassword,
                                      String name, String nickname,
                                      LocalDate birthDate, Gender gender,
                                      String phone, Address address) {
        return NormalMember.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)             // 부모(Member) 필드로 박힘
                .nickname(nickname)
                .birthDate(birthDate)
                .gender(gender)
                .phone(phone)
                .address(address)
                .memberStatus(MemberStatus.PENDING)
                .build();
    }

    public void changeMember(String nickname, String phone, Address address) {
        this.nickname = nickname;
        this.phone = phone;
        this.address = address;
    }

}
