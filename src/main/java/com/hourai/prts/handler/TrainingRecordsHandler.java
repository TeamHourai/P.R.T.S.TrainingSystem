package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

/*
  GET/POST/PUT/DELETE /api/v1/user/training-records

  认证：Authorization: Bearer user-{id}

  GET 返回：
    {"success":true,"records":{"1":{"attempts":2,"correct":true,"lastAt":1734960000000},...}}

  PUT/POST body:
    {"questionId":1,"attempts":2,"correct":true,"lastAt":1734960000000}

  DELETE：清空当前用户的培训记录
*/
public class TrainingRecordsHandler implements HttpHandler {
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
            Map<Long, DataStore.TrainingRecord> recs = DataStore.loadTrainingRecords(userId);
            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\":true,\"records\":{");
            boolean first = true;
            for (Map.Entry<Long, DataStore.TrainingRecord> e : recs.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                DataStore.TrainingRecord r = e.getValue();
                sb.append("\"").append(e.getKey()).append("\":{\"attempts\":").append(r.attempts)
                        .append(",\"correct\":").append(r.correct)
                        .append(",\"lastAt\":").append(r.lastAt).append("}");
            }
            sb.append("}}");
            Utils.send(exchange, 200, sb.toString());
            return;
        }

        if ("DELETE".equalsIgnoreCase(method)) {
            DataStore.clearTrainingRecords(userId);
            Utils.send(exchange, 200, "{\"success\":true}");
            return;
        }

        if ("PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)) {
            DataStore.TrainingRecord payload;
            try {
                payload = Utils.parseJson(exchange, DataStore.TrainingRecord.class);
            } catch (Exception e) {
                Utils.send(exchange, 400, "{\"success\":false,\"message\":\"invalid json\"}");
                return;
            }

            long qid = payload.questionId;
            int attempts = payload.attempts;
            boolean correct = payload.correct;
            long lastAt = payload.lastAt;
            if (qid <= 0) {
                Utils.send(exchange, 400, "{\"success\":false,\"message\":\"questionId required\"}");
                return;
            }
            if (attempts < 0) attempts = 0;
            if (lastAt <= 0) lastAt = System.currentTimeMillis();

            DataStore.TrainingRecord saved = DataStore.upsertTrainingRecord(userId, qid, attempts, correct, lastAt);
            Utils.send(exchange, 200,
                    "{\"success\":true,\"questionId\":" + qid + ",\"attempts\":" + saved.attempts + ",\"correct\":" + saved.correct + ",\"lastAt\":" + saved.lastAt + "}");
            return;
        }

        Utils.send(exchange, 405, "{\"success\":false,\"message\":\"GET/PUT/DELETE required\"}");
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

