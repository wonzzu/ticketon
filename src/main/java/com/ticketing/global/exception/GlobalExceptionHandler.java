package com.ticketing.global.exception;

import com.ticketing.global.BaseResponse;
import com.ticketing.global.BaseResponseStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException e) {
        BaseResponseStatus status = e.getStatus();
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.error(status));
    }
}


