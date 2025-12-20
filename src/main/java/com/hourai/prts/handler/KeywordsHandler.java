package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
        Path target = useOnboarding ? DataStore.getQuestionsFile().resolveSibling("questions_onboarding.csv") : DataStore.getQuestionsFile();

        if (!Files.exists(target)) {
            Utils.send(exchange, 200, "[]");
            return;
        }

        List<String> lines = Files.readAllLines(target, StandardCharsets.UTF_8);
        Set<String> kws = new LinkedHashSet<>();
        for (String ln : lines) {
            if (ln == null || ln.trim().isEmpty()) continue;
            // Accept both ASCII comma and full-width Chinese comma as column separators
            String[] p = ln.split("[,，]", 10);
            if (p.length >= 10) {
                String k = Utils.unescapeCsv(p[9]);
                if (!k.trim().isEmpty()) {
                    for (String part : k.split("\\|")) {
                        String t = part.trim();
                        if (!t.isEmpty()) kws.add(t);
                    }
                }
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
