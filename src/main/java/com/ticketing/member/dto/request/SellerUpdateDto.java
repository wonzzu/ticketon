package com.ticketing.member.dto.request;

import com.ticketing.global.entity.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SellerUpdateDto {

    @NotBlank
    private String phone;

    @NotNull
    private Address address;

    @NotBlank
    private String companyName;

    @NotBlank
    private String representativeName;

    @NotBlank
    private String businessNumber;

}
