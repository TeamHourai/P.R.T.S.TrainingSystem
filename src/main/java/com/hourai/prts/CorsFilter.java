package com.hourai.prts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

// 跨域过滤器：允许前端访问后端接口
public class CorsFilter implements HttpHandler {
    private final HttpHandler next; // 后续的业务处理器

    // 构造方法：传入真实的业务处理器
    public CorsFilter(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // 1) 必须用 set（不是 add），避免多次包装/多次调用时产生重复 header，导致浏览器判定无效
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization, X-Requested-With, Origin");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");

        // 注意：当 Allow-Origin 为 '*' 时，不能同时返回 Allow-Credentials:true，否则浏览器会直接拒绝
        // 所以这里不返回 Access-Control-Allow-Credentials

        // 2) 预检请求直接返回（不走业务 handler），并确保带上上面的 CORS 头
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // 3) 正常请求
        next.handle(exchange);
    }
}