package com.dootask.backend.common.exception;

public class ApiException extends RuntimeException {
    private Integer code;
    private String message;
    private Object data;

    public ApiException(String message) {
        super(message);
        this.message = message;
        this.code = 500;
    }

    public ApiException(String message, Integer code) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public ApiException(String message, Object data) {
        super(message);
        this.message = message;
        this.data = data;
        this.code = 500;
    }

    public ApiException(String message, Integer code, Object data) {
        super(message);
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}