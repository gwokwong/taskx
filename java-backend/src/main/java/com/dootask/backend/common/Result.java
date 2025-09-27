package com.dootask.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private Boolean ret;
    private String msg;
    private T data;

    public Result() {}

    public Result(Boolean ret, String msg, T data) {
        this.ret = ret;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(true, "success", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, "success", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(true, msg, data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(false, msg, null);
    }

    public static <T> Result<T> error(String msg, T data) {
        return new Result<>(false, msg, data);
    }
}