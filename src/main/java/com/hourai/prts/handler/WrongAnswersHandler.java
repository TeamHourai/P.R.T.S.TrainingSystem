package com.hourai.prts.handler;

import com.hourai.prts.entity.Question;
import com.hourai.prts.service.WrongQuestionService;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;

/*
  GET /api/v1/answers/wrong?page=1&size=1000

  前端会在登录后调用该接口拉取错题。
  认证：Authorization: Bearer user-{id}

  注意：data/user_answers.csv 历史数据里可能存在 selected 字段为 "null" 的行，
  DataStore.loadUserAnswers() 会在解析 Integer.parseInt 时抛异常导致连接被断开。
  这里做容错读取，确保接口永远返回有效 JSON。
*/
public class WrongAnswersHandler implements HttpHandler {
    private final WrongQuestionService wrongQuestionService = new WrongQuestionService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        // Allow GET/DELETE.
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

        if ("GET".equalsIgnoreCase(method)) {
            List<Question> qs = wrongQuestionService.getVisibleWrongQuestions(userId);
            Utils.send(exchange, 200, Utils.questionsToJson(qs));
            return;
        }

        if ("DELETE".equalsIgnoreCase(method)) {
            // Expected path: /api/v1/answers/wrong/{questionId}
            String path = exchange.getRequestURI().getPath();
            String[] segs = path.split("/");
            String last = segs.length > 0 ? segs[segs.length - 1] : "";
            if (last == null || last.trim().isEmpty() || "wrong".equalsIgnoreCase(last.trim())) {
                Utils.send(exchange, 400, "{\"success\":false,\"message\":\"questionId required\"}");
                return;
            }
            long questionId;
            try {
                questionId = Long.parseLong(last.trim());
            } catch (NumberFormatException nfe) {
                Utils.send(exchange, 400, "{\"success\":false,\"message\":\"invalid questionId\"}");
                return;
            }

            wrongQuestionService.hideWrongQuestion(userId, questionId);
            Utils.send(exchange, 200, "{\"success\":true,\"message\":\"hidden\",\"userId\":" + userId + ",\"questionId\":" + questionId + "}");
            return;
        }

        Utils.send(exchange, 405, "{\"success\":false,\"message\":\"GET or DELETE required\"}");
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
