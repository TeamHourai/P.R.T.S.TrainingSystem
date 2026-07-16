package com.hourai.prts.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一接口响应封装。
 *
 * <p>示例（成功）：
 * <pre>{@code
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { "id": 59 },
 *   "success": true
 * }
 * }</pre>
 *
 * <p>示例（失败）：
 * <pre>{@code
 * {
 *   "code": 401,
 *   "message": "未登录或登录已过期",
 *   "data": null,
 *   "success": false
 * }
 * }</pre>
 *
 * @param <T> 业务数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private boolean success;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = code == ResultCode.SUCCESS.getCode();
    }

    // ===== 成功 =====

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> success(ResultCode resultCode, T data) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), data);
    }

    // ===== 失败 =====

    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    // ===== Getter / Setter =====

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
        this.success = code == ResultCode.SUCCESS.getCode();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
