package com.ticketing.payment.controller;


import com.ticketing.auth.CustomUserDetails;
import com.ticketing.global.BaseResponse;
import com.ticketing.payment.dto.request.PaymentCreateDto;
import com.ticketing.payment.dto.response.PaymentResponseDto;
import com.ticketing.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<BaseResponse<PaymentResponseDto>> pay(
            @Validated @RequestBody PaymentCreateDto dto, @AuthenticationPrincipal CustomUserDetails user) {
        PaymentResponseDto pay = paymentService.pay(user.getMemberId(), dto);

        return ResponseEntity.status(CREATED).body(BaseResponse.success(pay));
    }

}
