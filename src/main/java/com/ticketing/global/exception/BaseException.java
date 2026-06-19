package com.ticketing.global.exception;

import com.ticketing.global.baseresponse.BaseResponseStatus;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final BaseResponseStatus baseResponseStatus;

    public BaseException(BaseResponseStatus status) {
        super(status.getMessage());
        this.baseResponseStatus = status;
    }
}
