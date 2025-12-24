package com.hourai.prts.handler;

// Exam history loaded from DB only
import com.hourai.prts.entity.ExamRecord;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
  GET /exam/history?page=1&size=10
  返回考试历史列表（按时间倒序）。

  为了兼容前端初始化调用：/api/v1/exam/history?page=1&size=100
*/
public class ExamHistoryHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange, 405, "{\"error\":\"GET required\"}");
            return;
        }

        URI uri = exchange.getRequestURI();
        Map<String, String> q = Utils.parseQuery(uri.getQuery());
        int page = parseIntOrDefault(q.get("page"), 1);
        int size = parseIntOrDefault(q.get("size"), 10);
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if (size > 1000) size = 1000;

        List<ExamRecord> all;
        try {
            com.hourai.prts.service.ExamRecordService examRecordService = new com.hourai.prts.service.ExamRecordService();
            all = examRecordService.getAllExamRecords();
        } catch (Exception dbEx) {
            dbEx.printStackTrace();
            Utils.send(exchange, 500, "{\"success\":false,\"message\":\"database error\"}");
            return;
        }
        // 按 createdAt 倒序（null 视为最早）
        List<ExamRecord> sorted = all.stream()
                .sorted(Comparator.comparing(ExamRecord::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());

        int from = Math.min((page - 1) * size, sorted.size());
        int to = Math.min(from + size, sorted.size());
        List<ExamRecord> pageList = sorted.subList(from, to);

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (ExamRecord r : pageList) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{");
            sb.append("\"examId\":").append(r.getId()).append(",");
            sb.append("\"userId\":").append(r.getUserId()).append(",");
            sb.append("\"score\":").append(r.getScore() == null ? 0 : r.getScore().intValue()).append(",");
            sb.append("\"createdAt\":\"")
                    .append(r.getCreatedAt() == null ? "" : Utils.escapeJson(r.getCreatedAt().toString()))
                    .append("\"");
            sb.append("}");
        }
        sb.append("]");

        Utils.send(exchange, 200, sb.toString());
    }

    private static int parseIntOrDefault(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }
}
