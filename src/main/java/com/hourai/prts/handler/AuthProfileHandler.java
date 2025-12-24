package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.hourai.prts.entity.User;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;

/*
  GET /api/v1/auth/profile

  前端用途：启动时根据 token 获取当前用户信息。
  本项目 token 目前是轻量占位（login 返回的 "user-{id}"），这里按该规则解析。
*/
public class AuthProfileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange, 405, "{\"success\":false,\"message\":\"GET required\"}");
            return;
        }

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.toLowerCase().startsWith("bearer ")) {
            Utils.send(exchange, 401, "{\"success\":false,\"message\":\"missing token\"}");
            return;
        }

        String token = auth.substring(7).trim();
        Long userId = parseUserIdFromToken(token);
        if (userId == null) {
            Utils.send(exchange, 401, "{\"success\":false,\"message\":\"invalid token\"}");
            return;
        }

        User found = null;
        try {
            com.hourai.prts.service.UserService userService = new com.hourai.prts.service.UserService();
            found = userService.getUserById(userId);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.send(exchange, 500, "{\"success\":false,\"message\":\"database error\"}");
            return;
        }
        if (found == null) {
            Utils.send(exchange, 401, "{\"success\":false,\"message\":\"user not found\"}");
            return;
        }

        String body = "{"
                + "\"id\":" + found.getId() + ","
                + "\"username\":\"" + Utils.escapeJson(found.getUsername()) + "\","
                + "\"isAdmin\":" + found.isAdmin()
                + "}";
        Utils.send(exchange, 200, body);
    }

    private static Long parseUserIdFromToken(String token) {
        if (token == null) return null;
        token = token.trim();
        if (token.startsWith("user-")) {
            try {
                return Long.parseLong(token.substring("user-".length()));
            } catch (Exception ignored) {
                return null;
            }
        }
        // 兼容纯数字 token
        try {
            return Long.parseLong(token);
        } catch (Exception ignored) {
            return null;
        }
    }
}
