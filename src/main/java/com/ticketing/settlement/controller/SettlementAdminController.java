package com.ticketing.settlement.controller;

import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.settlement.batch.SettlementScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/settlements")
public class SettlementAdminController {

    private final SettlementScheduler settlementScheduler;

    @PostMapping("/run")
    public ResponseEntity<BaseResponse<Void>> run(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                  LocalDate targetDate) {

        settlementScheduler.run(targetDate);

        return ResponseEntity.ok(BaseResponse.success());
    }

    @PostMapping("/reaggregate")
    public ResponseEntity<BaseResponse<Void>> reaggregate() {

        settlementScheduler.reaggregateDirty();

        return ResponseEntity.ok(BaseResponse.success());
    }
}
