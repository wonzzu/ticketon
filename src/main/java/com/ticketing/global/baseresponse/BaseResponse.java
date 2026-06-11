package com.ticketing.global.baseresponse;

import lombok.Getter;

@Getter
public class BaseResponse<T> {
    private boolean success;
    private int code;
    private String message;
    private T data;

    private BaseResponse(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, BaseResponseStatus.SUCCESS.getCode(), BaseResponseStatus.SUCCESS.getMessage(), data);
    }

    public static BaseResponse<Void> success() {
        return new BaseResponse<>(true, BaseResponseStatus.SUCCESS.getCode(), BaseResponseStatus.SUCCESS.getMessage(), null);
    }


    public static <T> BaseResponse<T> error(BaseResponseStatus status) {
        return new BaseResponse<>(false, status.getCode(), status.getMessage(), null);
    }
}