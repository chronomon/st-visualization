package com.jd.st.data.server.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应结果的统一封装类
 *
 * @param <T> 响应结果数据
 * @author wangrubin
 * @date 2022-08-02
 */
@Data
@NoArgsConstructor
public class ResponseResult<T> {
    private int code;
    private String message;
    private T data;
    /**
     * 业务日志
     */
    @JsonIgnore
    private String businessLog;

    public ResponseResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public String getBusinessLog() {
        return businessLog;
    }

    public void setBusinessLog(String businessLog) {
        this.businessLog = businessLog;
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
