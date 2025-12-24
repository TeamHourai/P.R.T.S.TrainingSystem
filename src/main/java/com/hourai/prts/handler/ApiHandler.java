package com.hourai.prts.handler;

import com.hourai.prts.dao.*;
import com.hourai.prts.entity.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ApiHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        String response = "";
        int status = 200;
        try {
            if ("/api/user".equals(path) && "POST".equals(method)) {
                // 用户注册/新增
                User user = parseUser(body);
                UserDao userDao = new UserDao();
                userDao.insert(user);
                response = "{\"userId\":" + user.getId() + ",\"message\":\"user success\"}";
            } else if ("/api/question".equals(path) && "POST".equals(method)) {
                // 新增试题
                Question question = parseQuestion(body);
                QuestionDao questionDao = new QuestionDao();
                questionDao.insert(question);
                response = "{\"questionId\":" + question.getId() + ",\"message\":\"question success\"}";
            } else if ("/api/exam_record".equals(path) && "POST".equals(method)) {
                // 新增考试记录
                ExamRecord record = parseExamRecord(body);
                ExamRecordDao examRecordDao = new ExamRecordDao();
                examRecordDao.insert(record);
                response = "exam_record success";
            } else if ("/api/user_answer".equals(path) && "POST".equals(method)) {
                // 新增答题记录
                UserAnswer answer = parseUserAnswer(body);
                UserAnswerDao userAnswerDao = new UserAnswerDao();
                userAnswerDao.insert(answer);
                response = "user_answer success";
            } else {
                status = 404;
                response = "Not Found";
            }
        } catch (Exception e) {
            status = 500;
            response = "Error: " + e.getMessage();
        }
        exchange.sendResponseHeaders(status, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    // 以下为简单 JSON 解析方法（可用第三方库替换）
    private User parseUser(String json) {
        // 简单 JSON 解析（建议生产环境用 Jackson/Gson）
        String username = extract(json, "username");
        String password = extract(json, "password");
        boolean isAdmin = Boolean.parseBoolean(extract(json, "isAdmin"));
        String createdAt = extract(json, "createdAt");
        if (createdAt.isEmpty()) createdAt = extract(json, "registerTime");
        return new User(null, username, password, isAdmin, createdAt);
    }
    private Question parseQuestion(String json) {
        Long id = null;
        int type = parseInt(extract(json, "type"));
        int difficulty = parseInt(extract(json, "difficulty"));
        String resource = extract(json, "resource");
        String question = extract(json, "question");
        boolean hasPicture = Boolean.parseBoolean(extract(json, "hasPicture"));
        String optionsRaw = extract(json, "options");
        if (optionsRaw.isEmpty()) optionsRaw = extract(json, "choices");
        java.util.List<String> options = java.util.Arrays.asList(optionsRaw.split("\\|").clone());
        int answer = parseInt(extract(json, "answer"));
        String analysis = extract(json, "analysis");
        return new Question(id, type, difficulty, resource, question, hasPicture, options, answer, analysis);
    }
    private ExamRecord parseExamRecord(String json) {
        System.out.println("[调试] parseExamRecord 收到 JSON: " + json);
        Long id = null;
        Long userId = parseLong(extract(json, "userId"));
        if (userId == null) userId = parseLong(extract(json, "user_id"));
        System.out.println("[调试] parseExamRecord 解析 userId: " + userId);
        int score = parseInt(extract(json, "score"));
        String completedAt = extract(json, "completedAt");
        if (completedAt.isEmpty()) completedAt = extract(json, "submitTime");
        return new ExamRecord(id, userId, score, completedAt);
    }
    private UserAnswer parseUserAnswer(String json) {
        System.out.println("[调试] parseUserAnswer 收到 JSON: " + json);
        Long id = null;
        Long userId = parseLong(extract(json, "userId"));
        if (userId == null) userId = parseLong(extract(json, "user_id"));
        System.out.println("[调试] parseUserAnswer 解析 userId: " + userId);
        Long questionId = parseLong(extract(json, "questionId"));
        if (questionId == null) questionId = parseLong(extract(json, "question_id"));
        System.out.println("[调试] parseUserAnswer 解析 questionId: " + questionId);
        String questionType = extract(json, "questionType");
        boolean isCorrect = Boolean.parseBoolean(extract(json, "isCorrect"));
        int selected = parseInt(extract(json, "selected"));
        if (selected == 0) selected = parseInt(extract(json, "selectedAnswer"));
        String answeredAt = extract(json, "answeredAt");
        if (answeredAt.isEmpty()) answeredAt = extract(json, "createdAt");
        return new UserAnswer(id, userId, questionId, questionType, isCorrect, selected, answeredAt);
    }

    // 简单 JSON 字段提取（仅支持扁平结构，生产建议用 Gson/Jackson）
    private String extract(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*([\"{\\[]?)([^\",{}\\[\\]]+)";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            return m.group(2);
        }
        return "";
    }
    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
    private Long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return null; }
    }
}
