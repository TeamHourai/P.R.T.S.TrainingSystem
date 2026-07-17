package com.hourai.prts.config;

import com.hourai.prts.util.InputSanitizer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局 XSS 清洗过滤器。
 *
 * <p>包装请求，对 query 参数与 form 参数做 {@link InputSanitizer#sanitize(String)} 清洗，
 * 防止通过 URL / 表单字段注入脚本。JSON 请求体不在此处解析（成本高且可能破坏流），
 * 其清洗在 Controller / Service 持久化前完成（见 InputSanitizer 的应用点）。
 *
 * <p>CORS 预检 OPTIONS 直接放行。
 */
public class XssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(new XssRequestWrapper(request), response);
    }

    /**
     * 请求包装器：对 getParameter / getParameterValues / getParameterMap 做清洗。
     */
    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        private final HttpServletRequest original;
        private Map<String, String[]> sanitizedParams;

        XssRequestWrapper(HttpServletRequest request) {
            super(request);
            this.original = request;
        }

        @Override
        public String getParameter(String name) {
            String value = original.getParameter(name);
            return InputSanitizer.sanitize(value);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = original.getParameterValues(name);
            if (values == null) return null;
            String[] out = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                out[i] = InputSanitizer.sanitize(values[i]);
            }
            return out;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            if (sanitizedParams == null) {
                Map<String, String[]> result = new LinkedHashMap<>();
                Enumeration<String> names = original.getParameterNames();
                while (names.hasMoreElements()) {
                    String name = names.nextElement();
                    result.put(name, getParameterValues(name));
                }
                sanitizedParams = result;
            }
            return sanitizedParams;
        }
    }
}
