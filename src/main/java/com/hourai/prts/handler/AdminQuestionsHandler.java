package com.hourai.prts.handler;

import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminQuestionsHandler implements HttpHandler {
    // Admin batch-delete operates on DB via QuestionService

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        // only support POST /admin/questions/batch-delete
        if ("POST".equalsIgnoreCase(method) && path != null && path.endsWith("/batch-delete")) {
            handleBatchDelete(exchange);
            return;
        }
        Utils.send(exchange, 405, "{\"error\":\"method not allowed\"}");
    }

    private void handleBatchDelete(HttpExchange exchange) throws IOException {
        // parse JSON body: expect { ids: [1,2,3] } or {"ids":"1,2,3"} or form encoded ids=1,2,3
        List<Long> ids = new ArrayList<>();

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String s = body.trim();

        // DEBUG: log received body to stdout for easier troubleshooting
        System.out.println("[AdminQuestionsHandler] received body: " + s);

        // Try to extract ids array robustly from JSON-like bodies
        List<Long> parsedFromJson = parseIdsFromJsonString(s);
        if (!parsedFromJson.isEmpty()) {
            ids.addAll(parsedFromJson);
        } else {
            // Fallback: try to parse form-encoded body (e.g., ids=1,2,3)
            Map<String, String> form = Utils.parseQuery(s);
            String idsStr = form.get("ids");
            if (idsStr != null && !idsStr.trim().isEmpty()) {
                for (String part : idsStr.split(",")) {
                    try { ids.add(Long.parseLong(part.trim())); } catch (Exception ignored) {}
                }
            }
        }

        // Final fallback: extract any integers present in the body (covers some edge cases)
        if (ids.isEmpty() && !s.isEmpty()) {
            Pattern numPat = Pattern.compile("\\d+");
            Matcher nm = numPat.matcher(s);
            while (nm.find()) {
                try { ids.add(Long.parseLong(nm.group())); } catch (Exception ignored) {}
            }
        }

        if (ids.isEmpty()) {
            // return 400 with the received body for easier debugging
            String resp = "{\"error\":\"no ids provided\",\"body\":\"" + Utils.escapeJson(s) + "\"}";
            Utils.send(exchange, 400, resp);
            return;
        }

        Set<Long> del = new HashSet<>(ids);
        com.hourai.prts.service.QuestionService qs = new com.hourai.prts.service.QuestionService();
        boolean anyDeleted = false;
        for (Long qid : del) {
            try {
                int changed = qs.deleteQuestion(qid);
                if (changed > 0) anyDeleted = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!anyDeleted) {
            Utils.send(exchange, 404, "{\"error\":\"no matching ids found\"}");
            return;
        }
        Utils.send(exchange, 200, "{\"success\":true}");
    }

    // Helper: find "ids" then locate the bracketed array and extract integers inside
    private List<Long> parseIdsFromJsonString(String s) {
        if (s == null || s.isEmpty()) return new ArrayList<>();
        String lower = s.toLowerCase();
        int idx = lower.indexOf("\"ids\"");
        if (idx < 0) idx = lower.indexOf("ids");
        if (idx < 0) return new ArrayList<>();
        int l = s.indexOf('[', idx);
        if (l < 0) return new ArrayList<>();
        int r = s.indexOf(']', l);
        if (r < 0 || r <= l) return new ArrayList<>();
        String inner = s.substring(l + 1, r);
        List<Long> out = new ArrayList<>();
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(inner);
        while (m.find()) {
            try { out.add(Long.parseLong(m.group())); } catch (Exception ignored) {}
        }
        return out;
    }
}
