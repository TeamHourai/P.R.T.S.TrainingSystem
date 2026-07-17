package com.hourai.prts.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 提取工具。
 *
 * <p>优先读取反向代理常用头（X-Forwarded-For / X-Real-IP），取第一个非 unknown 的地址；
 * 都缺失时回退到 {@link HttpServletRequest#getRemoteAddr()}。
 */
public final class IpUtils {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private IpUtils() {}

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        for (String header : IP_HEADERS) {
            String value = request.getHeader(header);
            if (value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value)) {
                // X-Forwarded-For 可能形如 "client, proxy1, proxy2"，取首个
                return value.split(",")[0].trim();
            }
        }
        String remote = request.getRemoteAddr();
        return remote != null ? remote : "unknown";
    }
}
