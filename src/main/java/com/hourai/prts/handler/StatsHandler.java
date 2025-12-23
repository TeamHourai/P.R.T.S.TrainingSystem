package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.hourai.prts.entity.UserAnswer;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Minimal stats endpoints under /stats, /api/stats and /api/v1/stats used by the frontend.
 *
 * Supported:
 *  - GET .../stats/question/{id}
 *  - GET .../stats/user
 *  - GET .../stats/system
 */
public class StatsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Utils.send(exchange, 405, "{\"error\":\"GET required\"}");
                return;
            }

            final String path = exchange.getRequestURI().getPath();

            if (path.contains("/stats/question/")) {
                handleQuestionStats(exchange, path);
                return;
            }

            if (path.contains("/stats/user")) {
                // Minimal placeholder; expand later if needed
                Utils.send(exchange, 200, "{\"totalAttempts\":0,\"correctRate\":0,\"totalUsers\":0}");
                return;
            }

            if (path.contains("/stats/system")) {
                Utils.send(exchange, 200, "{\"status\":\"ok\"}");
                return;
            }

            Utils.send(exchange, 404, "{\"error\":\"not found\"}");
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                Utils.send(exchange, 500, "{\"error\":\"internal server error\"}");
            } catch (Exception ignored) {}
        }
    }

    private void handleQuestionStats(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length == 0) {
            Utils.send(exchange, 400, "{\"error\":\"questionId missing\"}");
            return;
        }
        String idStr = parts[parts.length - 1];
        if (idStr == null || idStr.isBlank()) {
            Utils.send(exchange, 400, "{\"error\":\"questionId missing\"}");
            return;
        }

        long qid;
        try {
            qid = Long.parseLong(idStr);
        } catch (Exception e) {
            Utils.send(exchange, 400, "{\"error\":\"questionId invalid\"}");
            return;
        }

        List<UserAnswer> answers = DataStore.loadUserAnswers();
        int total = 0;
        int correct = 0;
        int[] wrongCounts = new int[10];

        for (UserAnswer ua : answers) {
            if (ua.getQuestionId() == null || ua.getQuestionId() != qid) continue;
            total++;
            if (ua.isCorrect()) {
                correct++;
            } else {
                int opt = 0;
                try {
                    opt = Integer.parseInt(String.valueOf(ua.getSelectedAnswer()));
                } catch (Exception ignore) {
                    opt = 0;
                }
                if (opt >= 0 && opt < wrongCounts.length) wrongCounts[opt]++;
            }
        }

        int mostWrongOpt = 0;
        int maxCount = 0;
        for (int i = 0; i < wrongCounts.length; i++) {
            if (wrongCounts[i] > maxCount) {
                maxCount = wrongCounts[i];
                mostWrongOpt = i;
            }
        }

        double correctRate = total == 0 ? 0.0 : (double) correct / (double) total;

        String json = "{"
                + "\"totalUsers\":" + total + ","
                + "\"correctRate\":" + String.format(Locale.US, "%.4f", correctRate) + ","
                + "\"mostCommonWrongOption\":" + mostWrongOpt
                + "}";

        Utils.send(exchange, 200, json);
    }
}
