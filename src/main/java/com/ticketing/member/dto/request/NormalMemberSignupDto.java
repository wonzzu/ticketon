package com.ticketing.member.dto.request;

import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class NormalMemberSignupDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private String password;

    @NotBlank
    private String phone;

    @NotBlank
    private String nickname;

    @NotNull
    private LocalDate birthDate;

    @NotNull
    private Gender gender;

    @NotNull
    private Address address;


}
