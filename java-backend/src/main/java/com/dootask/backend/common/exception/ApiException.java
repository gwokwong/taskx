package com.dootask.backend.common.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private Integer code;
    private String message;
    private Object data;
    private HttpStatus httpStatus;

    public ApiException(String message) {
        super(message);
        this.message = message;
        this.code = 500;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public ApiException(String message, Integer code) {
        super(message);
        this.message = message;
        this.code = code;
        this.httpStatus = HttpStatus.valueOf(code);
    }

    public ApiException(String message, Object data) {
        super(message);
        this.message = message;
        this.data = data;
        this.code = 500;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public ApiException(String message, Integer code, Object data) {
        super(message);
        this.message = message;
        this.code = code;
        this.data = data;
        this.httpStatus = HttpStatus.valueOf(code);
    }

    public ApiException(String message, HttpStatus httpStatus) {
        super(message);
        this.message = message;
        this.code = httpStatus.value();
        this.httpStatus = httpStatus;
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

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}