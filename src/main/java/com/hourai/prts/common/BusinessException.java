package com.hourai.prts.common;

/**
 * 业务异常：携带语义化 {@link ResultCode} 与提示信息，
 * 由 {@code GlobalExceptionHandler} 统一转换为标准响应体。
 *
 * <p>用法示例：
 * <pre>{@code
 * throw new BusinessException(ResultCode.CONFLICT, "该用户名已存在");
 * }</pre>
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
