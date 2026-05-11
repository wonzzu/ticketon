package com.ticketing.member.dto.request;

import com.ticketing.global.entity.Address;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SellerSignupDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

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
