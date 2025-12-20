package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.hourai.prts.entity.Question;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
  GET /api/v1/answers/wrong?page=1&size=1000

  前端会在登录后调用该接口拉取错题。
  认证：Authorization: Bearer user-{id}

  注意：data/user_answers.csv 历史数据里可能存在 selected 字段为 "null" 的行，
  DataStore.loadUserAnswers() 会在解析 Integer.parseInt 时抛异常导致连接被断开。
  这里做容错读取，确保接口永远返回有效 JSON。
*/
public class WrongAnswersHandler implements HttpHandler {
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

        Set<Long> qids = loadWrongQuestionIds(userId);
        List<Question> qs = DataStore.loadQuestions().stream().filter(q -> qids.contains(q.getId())).collect(Collectors.toList());
        Utils.send(exchange, 200, Utils.questionsToJson(qs));
    }

    private static Set<Long> loadWrongQuestionIds(Long userId) throws IOException {
        Set<Long> qids = new LinkedHashSet<>();
        java.nio.file.Path file = java.nio.file.Paths.get("data").resolve("user_answers.csv");
        if (!Files.exists(file)) return qids;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (String ln : lines) {
            if (ln == null) continue;
            String t = ln.trim();
            if (t.isEmpty()) continue;
            // 旧格式: id,userId,questionId,normal,true,2,2025-...
            // 新/脏数据: id,userId,questionId,2,false,null,2025-...
            String[] p = t.split(",", 7);
            if (p.length < 5) continue;
            try {
                long uid = Long.parseLong(p[1].trim());
                if (!userId.equals(uid)) continue;
                long qid = Long.parseLong(p[2].trim());
                boolean correct = Boolean.parseBoolean(p[4].trim());
                if (!correct) qids.add(qid);
            } catch (Exception ignored) {
                // 忽略坏行
            }
        }
        return qids;
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
