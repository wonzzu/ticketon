package com.ticketing.s3.service;


import com.ticketing.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

import static com.ticketing.global.baseresponse.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket:ticketon-posters}")
    private String bucket;

    @Value("${aws.s3.region:ap-northeast-2}")
    private String region;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    public String uploadPoster(MultipartFile file) {

        validate(file);

        if (!s3Enabled) {
            // 로컬(IAM 역할 없음): 실제 업로드 없이 프론트 public의 placeholder 반환
            return "/images/poster-placeholder.svg";
        }

        String key = "posters/" + UUID.randomUUID() + getExtension(file.getOriginalFilename());

        try {

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new BaseException(FILE_UPLOAD_FAILED);
        }


        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, key);
    }




    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException(FILE_EMPTY);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BaseException(FILE_NOT_IMAGE);
        }
        if (file.getSize() > 5 * 1024 * 1024) {   // 5MB 제한
            throw new BaseException(FILE_TOO_LARGE);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
