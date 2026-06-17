package com.ticketing.global.exception;

import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.global.baseresponse.BaseResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException e) {
        BaseResponseStatus status = e.getStatus();
        log.warn("비즈니스 예외: code={}, message={}", status.getCode(), status.getMessage());
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.error(status));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String detail = (fieldError == null)
                ? "유효성 검증 실패"
                : fieldError.getField() + ": " + fieldError.getDefaultMessage();
        log.warn("입력값 검증 실패: {}", detail);
        return ResponseEntity
                .status(BaseResponseStatus.INVALID_INPUT.getHttpStatus())
                .body(BaseResponse.error(BaseResponseStatus.INVALID_INPUT));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(Exception e) {
        log.error("예상치 못한 서버 오류", e);
        return ResponseEntity
                .status(BaseResponseStatus.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(BaseResponse.error(BaseResponseStatus.INTERNAL_SERVER_ERROR));
    }
}
