package com.ticketing.global.baseresponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BaseResponseStatus {

    // 공통 1000
    SUCCESS(true, 1000, "성공", HttpStatus.OK),

    // 회원 2000
    DUPLICATE_EMAIL(false, 2001, "이미 사용중인 이메일입니다", HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME(false, 2002, "이미 사용중인 닉네임입니다", HttpStatus.CONFLICT),
    MEMBER_NOT_FOUND(false, 2003, "존재하지 않는 회원입니다", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(false, 2004, "비밀번호가 일치하지 않습니다", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_ACCESS(false, 2005, "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
    FORBIDDEN_ACCESS(false, 2006, "접근 권한이 없습니다", HttpStatus.FORBIDDEN),
    MEMBER_ALREADY_DELETED(false, 2007, "이미 탈퇴한 회원입니다", HttpStatus.BAD_REQUEST),

    // 공연 3000
    PERFORMANCE_NOT_FOUND(false, 3001, "존재하지 않는 공연입니다", HttpStatus.NOT_FOUND),
    PERFORMANCE_ALREADY_CLOSED(false, 3002, "이미 종료된 공연입니다", HttpStatus.BAD_REQUEST),
    INVALID_EVENT_STATUS(false, 3003, "현재 상태에서는 처리할 수 없습니다", HttpStatus.BAD_REQUEST),
    VENUE_NOT_FOUND(false, 3004, "존재하지 않는 공연장입니다", HttpStatus.NOT_FOUND),
    EVENT_ALREADY_REVIEWED(false, 3005, "이미 검수가 완료된 공연입니다", HttpStatus.CONFLICT),
    REJECT_REASON_REQUIRED(false, 3006, "반려 사유는 필수입니다", HttpStatus.BAD_REQUEST),
    EVENT_NOT_OWNED(false, 3007, "본인이 등록한 공연만 수정할 수 있습니다", HttpStatus.FORBIDDEN),

    // 예매 4000
    RESERVATION_NOT_FOUND(false, 4001, "존재하지 않는 예매입니다", HttpStatus.NOT_FOUND),
    INVALID_RESERVATION_STATUS(false, 4002, "현재 상태에서는 처리할 수 없습니다", HttpStatus.BAD_REQUEST),
    SEAT_NOT_AVAILABLE(false, 4003, "이미 선택된 좌석입니다", HttpStatus.CONFLICT),
    EXCEED_SEAT_LIMIT(false, 4004, "최대 3개 좌석까지 예매할 수 있습니다", HttpStatus.BAD_REQUEST),
    RESERVATION_NOT_OWNED(false, 4005, "본인 예매만 처리할 수 있습니다", HttpStatus.FORBIDDEN),
    EMPTY_SEAT_SELECTION(false, 4006, "좌석을 선택해주세요", HttpStatus.BAD_REQUEST),
    SEAT_HOLD_EXPIRED(false, 4007, "선점 시간이 만료되었습니다..", HttpStatus.BAD_REQUEST),


    // 결제 5000
    PAYMENT_FAILED(false, 5001, "결제에 실패했습니다", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND(false, 5002, "존재하지 않는 결제입니다", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_COMPLETED(false, 5003, "이미 완료된 결제입니다", HttpStatus.CONFLICT),

    // 대기열 6000
    QUEUE_NOT_FOUND(false, 6001, "대기열 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    QUEUE_ALREADY_ENTERED(false, 6002, "이미 대기열에 등록되어 있습니다", HttpStatus.CONFLICT),
    QUEUE_EXPIRED(false, 6003, "대기열이 만료되었습니다", HttpStatus.BAD_REQUEST),
    QUEUE_NOT_ADMITTED(false, 6004, "대기열을 먼저 통과해야 합니다", HttpStatus.FORBIDDEN),

    // 리뷰 7000
    REVIEW_NOT_FOUND(false, 7001, "존재하지 않는 후기입니다", HttpStatus.NOT_FOUND),
    INVALID_REVIEW_RATING(false, 7002, "별점은 1~5 사이여야 합니다", HttpStatus.BAD_REQUEST),
    DUPLICATE_REVIEW(false, 7003, "이미 작성한 후기가 있습니다", HttpStatus.CONFLICT),
    REVIEW_NOT_OWNED(false, 7004, "본인이 작성한 후기만 삭제할 수 있습니다", HttpStatus.FORBIDDEN),

    // 쿠폰 8000
    COUPON_NOT_FOUND(false, 8001, "존재하지 않는 쿠폰입니다", HttpStatus.NOT_FOUND),
    COUPON_ALREADY_ISSUED(false, 8002, "이미 발급받은 쿠폰입니다", HttpStatus.CONFLICT),
    COUPON_SOLD_OUT(false, 8003, "쿠폰이 모두 소진되었습니다", HttpStatus.CONFLICT),

    // 서버 9000
    INTERNAL_SERVER_ERROR(false, 9001, "서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT(false, 9002, "입력값이 올바르지 않습니다", HttpStatus.BAD_REQUEST),
    TOO_MANY_REQUESTS(false, 9003, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요", HttpStatus.TOO_MANY_REQUESTS),;

    private final boolean success;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
