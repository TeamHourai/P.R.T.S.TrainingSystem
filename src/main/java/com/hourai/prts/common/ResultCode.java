package com.hourai.prts.common;

/**
 * 统一接口状态码。
 *
 * <p>所有 REST 接口均返回 {@code {code, message, data, success}} 结构，其中：
 * <ul>
 *   <li>{@code code} 为业务/HTTP 语义状态码（见下表）</li>
 *   <li>{@code message} 为人类可读的提示信息</li>
 *   <li>{@code data} 为业务数据负载（成功时存在，失败时通常为 null）</li>
 *   <li>{@code success} 为 {@code code == 200} 派生的布尔值</li>
 * </ul>
 *
 * <p>约定：成功统一使用 {@link #SUCCESS}(200)；业务失败优先使用语义化状态码
 * （400/401/403/404/409），未预期错误使用 {@link #INTERNAL_ERROR}(500)。
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限执行该操作"),
    NOT_FOUND(404, "请求的资源不存在"),
    CONFLICT(409, "资源状态冲突，请刷新后重试"),

    INTERNAL_ERROR(500, "服务器内部错误，请稍后重试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
