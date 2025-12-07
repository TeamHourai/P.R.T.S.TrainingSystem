package com.hourai.prts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
// 新增 Utils 引用以统一响应发送
import com.hourai.prts.utils.Utils;

// 跨域过滤器：允许前端访问后端接口
public class CorsFilter implements HttpHandler {
    private final HttpHandler next; // 后续的业务处理器

    // 构造方法：传入真实的业务处理器
    public CorsFilter(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // 1. 设置跨域响应头（开发环境允许所有来源，生产环境可指定具体域名）
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        // 允许常见的请求头（Content-Type / Accept / Authorization / X-Requested-With / Origin）
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization, X-Requested-With, Origin");
        // 如需支持 cookie/凭证，可将此项改为 true 并设置具体域名而非 '*'
        exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
        // 预检缓存时间
        exchange.getResponseHeaders().add("Access-Control-Max-Age", "3600");

        // 2. 处理预检请求（OPTIONS请求：浏览器发送的跨域预热请求）
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            // 使用 Utils.send 统一返回 JSON 响应（空对象），保证 Content-Type 等头一致
            Utils.send(exchange, 200, "{}");
            return;
        }

        // 3. 继续处理真实的业务请求（如GET/POST）
        next.handle(exchange);
    }
}