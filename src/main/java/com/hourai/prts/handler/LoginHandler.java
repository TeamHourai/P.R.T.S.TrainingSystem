package com.hourai.prts.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hourai.prts.utils.Utils;
import com.hourai.prts.entity.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
  POST /login  body: username & password
*/
public class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange, 405, "{\"success\":false,\"message\":\"POST required\"}");
            return;
        }

        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        Map<String, String> params;
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            // 兼容前端 JSON 提交（apiapp.js 的 postJson）
            String body = new String(exchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            params = Utils.parseJsonObject(body);
        } else {
            // 原有：application/x-www-form-urlencoded
            params = Utils.parseForm(exchange);
        }

        String username = params.get("username");
        String password = params.get("password");
        if (username == null || password == null) {
            Utils.send(exchange, 400, "{\"success\":false,\"message\":\"username & password required\"}");
            return;
        }

        // 使用数据库进行校验（不再回退到本地 CSV）
        com.hourai.prts.service.UserService userService = new com.hourai.prts.service.UserService();
        User matchedUser = null;
        try {
            List<User> users = userService.getAllUsers();
            java.util.Optional<User> ou = users.stream()
                    .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                    .findFirst();
            if (ou.isPresent()) {
                matchedUser = ou.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = Utils.escapeJson(e.getClass().getSimpleName() + ": " + e.getMessage());
            Utils.send(exchange, 500, "{\"success\":false,\"message\":" + msg + "}");
            return;
        }
        if (matchedUser != null) {
            User u = matchedUser;
            String token = "user-" + u.getId();
            String body = "{"
                    + "\"success\":true,"
                    + "\"message\":\"登录成功\","
                    + "\"token\":\"" + Utils.escapeJson(token) + "\","
                    + "\"user\":{"
                    + "\"id\":" + u.getId() + ","
                    + "\"username\":\"" + Utils.escapeJson(u.getUsername()) + "\","
                    + "\"isAdmin\":" + u.isAdmin()
                    + "}"
                    + "}";

            Utils.send(exchange, 200, body);
        } else {
            Utils.send(exchange, 401, "{\"success\":false,\"message\":\"invalid credentials\"}");
        }
    }
}