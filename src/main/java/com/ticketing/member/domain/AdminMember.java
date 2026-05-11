package com.ticketing.member.domain;

import com.ticketing.global.entity.Address;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@DiscriminatorValue("ADMIN")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class AdminMember extends Member {

    @Column(nullable = false)
    private String department;

    @Column(nullable = false, unique = true)
    private String employeeNumber;

    public static AdminMember create(String email, String encodedPassword, String name,
                                     String phone, Address address,
                                     String department, String employeeNumber) {
        return AdminMember.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)             // 운영자 본인 이름
                .phone(phone)
                .address(address)
                .department(department)
                .employeeNumber(employeeNumber)
                .memberStatus(MemberStatus.ACTIVE)
                .build();
    }
}