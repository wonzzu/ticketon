package com.ticketing.s3.controller;

import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.s3.service.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "포스터 업로드")
@RestController
@RequiredArgsConstructor
public class PosterUploadController {

    private final S3UploadService s3UploadService;

    @Operation(summary = "포스터 이미지 업로드")
    @PostMapping("/uploads/poster")
    public ResponseEntity<BaseResponse<String>> uploadPoster(@RequestParam("file") MultipartFile file) {

        String url = s3UploadService.uploadPoster(file);

        return ResponseEntity.ok(BaseResponse.success(url));
    }
}
