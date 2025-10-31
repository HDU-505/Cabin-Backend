package com.hdu.exception;

public enum ErrorCode implements IErrorCode {
    // 实验相关异常代码
    EXPERIMENT_ERROR(10001,"实验发生不可逆转异常!");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public Integer getCode() { return code; }
    @Override public String getMessage() { return message; }
}