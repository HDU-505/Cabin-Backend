package com.hdu.exception;

public class ExperimentException extends RuntimeException{
    private final Integer code;
    private final String message;

    public ExperimentException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public ExperimentException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    // getters
    public Integer getCode() { return code; }
    @Override public String getMessage() { return message; }
}
