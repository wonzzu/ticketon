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
@DiscriminatorValue(value = "SELLER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Seller extends Member {

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String representativeName;

    @Column(nullable = false, unique = true)
    private String businessNumber;


    public static Seller create(String email, String encodedPassword, String name,
                                String phone, Address address,
                                String companyName, String representativeName, String businessNumber) {
        return Seller.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)             // 가입자 본인 이름 (대표자명과 별개)
                .phone(phone)
                .address(address)
                .companyName(companyName)
                .representativeName(representativeName)
                .businessNumber(businessNumber)
                .memberStatus(MemberStatus.PENDING)
                .build();
    }


    public void update(String phone, Address address,
                       String companyName, String representativeName, String businessNumber) {
        this.phone = phone;
        this.address = address;
        this.companyName = companyName;
        this.representativeName = representativeName;
        this.businessNumber = businessNumber;
    }
}
