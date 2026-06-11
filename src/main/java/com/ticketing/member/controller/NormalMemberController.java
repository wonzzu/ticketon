package com.ticketing.member.controller;

import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.global.ratelimit.RateLimit;
import com.ticketing.member.dto.request.MemberUpdateDto;
import com.ticketing.member.dto.request.NormalMemberSignupDto;
import com.ticketing.member.dto.response.NormalMemberResponseDto;
import com.ticketing.member.service.NormalMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class NormalMemberController {

    private final NormalMemberService normalMemberService;

    @RateLimit(key = RateLimit.KeyType.IP, limit = 20, windowSeconds = 60)
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<Void>> signup(@Validated @RequestBody NormalMemberSignupDto dto) {
        normalMemberService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<NormalMemberResponseDto>> getMember(@PathVariable Long id) {
        NormalMemberResponseDto data = normalMemberService.findById(id);
        return ResponseEntity.ok(BaseResponse.success(data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> update(@PathVariable Long id, @Validated @RequestBody MemberUpdateDto dto) {
        normalMemberService.update(id, dto);
        return ResponseEntity.ok(BaseResponse.success());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        normalMemberService.delete(id);
        return ResponseEntity.ok(BaseResponse.success());
    }
}
