package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/*
  GET/PUT /api/v1/user/answer-settings

  认证：Authorization: Bearer user-{id}

  返回示例：
    {"success":true,"autoSubmit":false,"autoNextCorrect":false}

  更新示例：
    PUT body (application/json): {"autoSubmit":true,"autoNextCorrect":false}
*/
public class AnswerSettingsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
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

        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            DataStore.AnswerSettings s = DataStore.loadAnswerSettings(userId);
            Utils.send(exchange, 200,
                    "{\"success\":true,\"autoSubmit\":" + s.autoSubmit + ",\"autoNextCorrect\":" + s.autoNextCorrect + "}");
            return;
        }

        if ("PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)) {
            DataStore.AnswerSettings payload;
            try {
                payload = Utils.parseJson(exchange, DataStore.AnswerSettings.class);
            } catch (Exception e) {
                Utils.send(exchange, 400, "{\"success\":false,\"message\":\"invalid json\"}");
                return;
            }

            boolean autoSubmit = payload.autoSubmit;
            boolean autoNextCorrect = payload.autoNextCorrect;

            DataStore.AnswerSettings saved = DataStore.upsertAnswerSettings(userId, autoSubmit, autoNextCorrect);
            Utils.send(exchange, 200,
                    "{\"success\":true,\"autoSubmit\":" + saved.autoSubmit + ",\"autoNextCorrect\":" + saved.autoNextCorrect + "}");
            return;
        }

        Utils.send(exchange, 405, "{\"success\":false,\"message\":\"GET/PUT required\"}");
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
        try {
            return Long.parseLong(token);
        } catch (Exception ignored) {
            return null;
        }
    }
}
