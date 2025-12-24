package com.hourai.prts.handler;

import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.*;

public class KeywordsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            handleGet(exchange);
            return;
        }
        Utils.send(exchange, 405, "{\"error\":\"method not allowed\"}");
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        Map<String, String> q = Utils.parseQuery(exchange.getRequestURI().getQuery());
        String mode = q.getOrDefault("mode", "");
        String fullPath = exchange.getRequestURI().getPath() == null ? "" : exchange.getRequestURI().getPath();
        boolean useOnboarding = "onboarding".equalsIgnoreCase(mode) || fullPath.toLowerCase().contains("/training/");
        java.util.List<com.hourai.prts.entity.Question> all;
        try {
            com.hourai.prts.service.QuestionService questionService = new com.hourai.prts.service.QuestionService();
            if (useOnboarding) {
                all = questionService.getAllQuestionsByType(2);
            } else {
                all = questionService.getAllQuestions();
            }
        } catch (Exception dbEx) {
            dbEx.printStackTrace();
            Utils.send(exchange, 500, "{\"success\":false,\"message\":\"database error\"}");
            return;
        }

        Set<String> kws = new LinkedHashSet<>();
        for (com.hourai.prts.entity.Question question : all) {
            String k = question.getKeywords();
            if (k == null || k.trim().isEmpty()) continue;
            for (String part : k.split("\\|")) {
                String t = part.trim();
                if (!t.isEmpty()) kws.add(t);
            }
        }

        // return as JSON array
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (String k : kws) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(Utils.escapeJson(k)).append('"');
        }
        sb.append("]");
        Utils.send(exchange, 200, sb.toString());
    }
}
