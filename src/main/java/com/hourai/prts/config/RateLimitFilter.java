package com.hourai.prts.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hourai.prts.common.Result;
import com.hourai.prts.common.ResultCode;
import com.hourai.prts.util.IpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

/**
 * 全局请求频率限制过滤器。
 *
 * <p>按「客户端 IP + 端点分组」做 60 秒滑动窗口计数：
 * <ul>
 *   <li>auth（登录/注册）：20 次/分钟 —— 防爆破</li>
 *   <li>admin（管理操作）：300 次/分钟</li>
 *   <li>default（其余）：600 次/分钟（约 10 次/秒，保证 SPA 首屏并发不触发）</li>
 * </ul>
 * 超限时返回 HTTP 429 与统一 {@link Result} 信封（code=429）。
 *
 * <p>纯内存实现，无外部依赖；适合单实例部署。多实例需替换为 Redis 共享计数。
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000L;

    // 阈值说明：SPA 首屏会并发若干请求，阈值需保证正常浏览/开发不触发，
    // 同时仍能拦截真正的刷接口/爆破。多实例需改 Redis 共享计数。
    private static final int LIMIT_AUTH = 20;     // 登录/注册：防爆破
    private static final int LIMIT_ADMIN = 300;   // 管理操作
    private static final int LIMIT_DEFAULT = 600; // 通用浏览（约 10 次/秒）

    private final ConcurrentMap<String, Deque<Long>> counters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 放行 CORS 预检
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = IpUtils.getClientIp(request);
        String tier = tierOf(request.getRequestURI());
        String key = ip + "|" + tier;

        if (isRateLimited(key, limitForTier(tier))) {
            writeTooManyRequests(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String tierOf(String uri) {
        if (uri == null) return "default";
        if (uri.contains("/auth/login") || uri.contains("/auth/register")) return "auth";
        if (uri.contains("/admin/")) return "admin";
        return "default";
    }

    private int limitForTier(String tier) {
        return switch (tier) {
            case "auth" -> LIMIT_AUTH;
            case "admin" -> LIMIT_ADMIN;
            default -> LIMIT_DEFAULT;
        };
    }

    /**
     * 滑动窗口判定：清理 60 秒之前的记录，若窗口内已达上限则拒绝。
     */
    private boolean isRateLimited(String key, int limit) {
        long now = System.currentTimeMillis();
        Deque<Long> deque = counters.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        synchronized (deque) {
            // 淘汰过期记录
            while (!deque.isEmpty() && now - deque.peekFirst() > WINDOW_MS) {
                deque.pollFirst();
            }
            if (deque.size() >= limit) {
                return true;
            }
            deque.addLast(now);
        }
        return false;
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<Void> body = Result.fail(ResultCode.TOO_MANY_REQUESTS);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
