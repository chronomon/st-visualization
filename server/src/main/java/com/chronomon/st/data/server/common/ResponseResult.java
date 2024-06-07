package com.chronomon.st.data.server.common;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应结果的统一封装类
 *
 * @param <T> 响应结果数据
 * @author wangrubin
 */
@Data
@NoArgsConstructor
public class ResponseResult<T> {
    private int code;

    private String message;

    private T data;

    public ResponseResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseResult<T> success(T data) {
        return success("success", data);
    }

    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>(200, message, data);
    }

    public static <T> ResponseResult<T> fail(String message) {
        return fail(500, message);
    }

    public static <T> ResponseResult<T> fail(Integer code, String message) {
        return new ResponseResult<>(code, message, null);
    }
}
