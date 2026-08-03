package com.ticketing.global.exception;

import com.ticketing.global.baseresponse.BaseResponse;
import com.ticketing.global.baseresponse.BaseResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException e) {
        BaseResponseStatus status = e.getBaseResponseStatus();
        log.warn("비즈니스 예외: code={}, message={}", status.getCode(), status.getMessage());
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.error(status));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Void>> handleAuthentication(AuthenticationException e) {
        log.warn("인증 실패: {}", e.getClass().getSimpleName());
        return ResponseEntity
                .status(BaseResponseStatus.INVALID_PASSWORD.getHttpStatus())
                .body(BaseResponse.error(BaseResponseStatus.INVALID_PASSWORD));
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("무결성 제약 위반: {}", e.getMostSpecificCause().getMessage());
        return ResponseEntity
                .status(BaseResponseStatus.DUPLICATE_REQUEST.getHttpStatus())
                .body(BaseResponse.error(BaseResponseStatus.DUPLICATE_REQUEST));
    }


    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<BaseResponse<Void>> handleOptimisticLock(ObjectOptimisticLockingFailureException e) {
        log.warn("좌석 선점 경합(낙관적 락): {}", e.getMessage());
        return ResponseEntity
                .status(BaseResponseStatus.SEAT_ALREADY_RESERVED.getHttpStatus())
                .body(BaseResponse.error(BaseResponseStatus.SEAT_ALREADY_RESERVED));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleUnexpected(Exception e) {
        log.error("예상치 못한 서버 오류", e);
        return ResponseEntity
                .status(BaseResponseStatus.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(BaseResponse.error(BaseResponseStatus.INTERNAL_SERVER_ERROR));
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        FieldError fieldError = ex.getBindingResult().getFieldError();
        String detail = (fieldError == null)
                ? "유효성 검증 실패"
                : fieldError.getField() + ": " + fieldError.getDefaultMessage();
        log.warn("입력값 검증 실패: {}", detail);

        return ResponseEntity
                .status(status)
                .body(BaseResponse.error(BaseResponseStatus.INVALID_INPUT));
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.warn("업로드 용량 초과: {}", ex.getMessage());

        return ResponseEntity
                .status(BaseResponseStatus.FILE_TOO_LARGE.getHttpStatus())
                .body(BaseResponse.error(BaseResponseStatus.FILE_TOO_LARGE));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        log.warn("요청 처리 실패: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());

        BaseResponseStatus status = statusCode.is4xxClientError()
                ? BaseResponseStatus.INVALID_INPUT
                : BaseResponseStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(statusCode)
                .body(BaseResponse.error(status));
    }
}
