package com.ticketing.global.exception;

import com.ticketing.global.baseresponse.BaseResponseStatus;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final BaseResponseStatus status;

    public BaseException(BaseResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }
}
