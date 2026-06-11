package com.ticketing.member.controller;

import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.member.dto.response.MyInfoResponseDto;
import com.ticketing.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/me")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<BaseResponse<MyInfoResponseDto>> myInfo(@AuthenticationPrincipal CustomUserDetails user) {
        MyInfoResponseDto myInfo = memberService.getMyInfo(user.getMemberId());

        return ResponseEntity.ok(BaseResponse.success(myInfo));
    }
}
