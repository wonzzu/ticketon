package com.ticketing.member.controller;

import com.ticketing.global.BaseResponse;
import com.ticketing.member.dto.request.SellerSignupDto;
import com.ticketing.member.dto.request.SellerUpdateDto;
import com.ticketing.member.dto.response.SellerResponseDto;
import com.ticketing.member.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sellers")
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<Void>> signup(@Validated @RequestBody SellerSignupDto dto) {
        sellerService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<SellerResponseDto>> getSeller(@PathVariable Long id) {
        return ResponseEntity.ok(BaseResponse.success(sellerService.findById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> update(@PathVariable Long id, @Validated @RequestBody SellerUpdateDto dto) {
        sellerService.update(id, dto);
        return ResponseEntity.ok(BaseResponse.success());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        sellerService.delete(id);
        return ResponseEntity.ok(BaseResponse.success());
    }
}
