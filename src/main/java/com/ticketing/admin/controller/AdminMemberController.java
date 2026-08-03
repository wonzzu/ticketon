package com.ticketing.admin.controller;


import com.ticketing.admin.dto.request.MemberSuspendDto;
import com.ticketing.admin.dto.response.AdminMemberDetailResponseDto;
import com.ticketing.admin.dto.response.AdminMemberListResponseDto;
import com.ticketing.admin.service.AdminMemberService;
import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.member.domain.MemberStatus;
import com.ticketing.member.domain.MemberType;
import com.ticketing.member.dto.request.MemberSearchCond;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 - 회원")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<AdminMemberListResponseDto>>> search(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) MemberStatus memberStatus,
            @RequestParam(required = false) MemberType memberType,
            @PageableDefault(size = 20) Pageable pageable) {

        MemberSearchCond cond = new MemberSearchCond(email, name, memberStatus, memberType);

        Page<AdminMemberListResponseDto> search = adminMemberService.search(cond, pageable);

        return ResponseEntity.ok(BaseResponse.success(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<AdminMemberDetailResponseDto>> detail(@PathVariable Long id) {
        AdminMemberDetailResponseDto detail = adminMemberService.findDetail(id);

        return ResponseEntity.ok(BaseResponse.success(detail));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<BaseResponse<Void>> suspend(
            @PathVariable Long id, @Validated @RequestBody MemberSuspendDto dto) {

        adminMemberService.suspend(id, dto.getReason());

        return ResponseEntity.ok(BaseResponse.success());
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<BaseResponse<Void>> release(@PathVariable Long id) {

        adminMemberService.release(id);

        return ResponseEntity.ok(BaseResponse.success());
    }
}