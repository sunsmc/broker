package com.broker.bo;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class HttpResult<T> {

    private Integer code;
    private String msg;
    private T data;

    public static <T> HttpResult<T> success() {
        return success(null);
    }

    public static <T> HttpResult<T> success(T data) {

        return success("success", data);
    }

    public static <T> HttpResult<T> success(String msg, T data) {

        return build(HttpStatus.OK.value(), msg, data);
    }

    public static <T> HttpResult<T> failure(String msg) {

        return build(HttpStatus.INTERNAL_SERVER_ERROR.value(), "failure", null);
    }

    public static <T> HttpResult<T> build(Integer code, String msg, T data) {

        return (HttpResult<T>) HttpResult.builder().code(code).msg(msg).data(data).build();
    }
}
