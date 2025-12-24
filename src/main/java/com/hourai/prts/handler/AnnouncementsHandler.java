package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.hourai.prts.entity.Announcement;
import com.hourai.prts.entity.User;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Announcements API.
 *
 * Public:
 *   GET /api/v1/announcements
 *
 * Admin:
 *   POST /api/v1/admin/announcements
 */
public class AnnouncementsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // route
        if (path.contains("/admin/")) {
            if (!"POST".equalsIgnoreCase(method)) {
                Utils.send(exchange, 405, "{\"success\":false,\"message\":\"POST required\"}");
                return;
            }
            handleAdminCreate(exchange);
            return;
        }

        if (!"GET".equalsIgnoreCase(method)) {
            Utils.send(exchange, 405, "{\"success\":false,\"message\":\"GET required\"}");
            return;
        }
        handleList(exchange);
    }

    private void handleList(HttpExchange exchange) throws IOException {
        List<Announcement> items = loadAll();

        // newest first
        items.sort((a, b) -> Long.compare(b.getId(), a.getId()));

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\":true,");
        sb.append("\"announcements\":[");
        for (int i = 0; i < items.size(); i++) {
            Announcement a = items.get(i);
            if (i > 0) sb.append(',');
            sb.append(toJson(a));
        }
        sb.append("]}");

        Utils.send(exchange, 200, sb.toString());
    }

    private void handleAdminCreate(HttpExchange exchange) throws IOException {
        // admin check
        User u = resolveUser(exchange);
        if (u == null || !u.isAdmin()) {
            Utils.send(exchange, 403, "{\"success\":false,\"message\":\"admin required\"}");
            return;
        }

        Map<String, String> params;
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        contentType = contentType == null ? "" : contentType.toLowerCase();
        if (contentType.contains("application/json")) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            params = Utils.parseJsonObject(body);
        } else {
            params = Utils.parseForm(exchange);
        }

        String type = safe(params.get("type"));
        if (type.isEmpty()) type = "system";
        String title = safe(params.get("title"));
        String content = safe(params.get("content"));
        boolean important = "true".equalsIgnoreCase(safe(params.get("important"))) || "1".equals(safe(params.get("important")));
        String expiresAt = safe(params.get("expiresAt"));

        if (title.isEmpty()) {
            Utils.send(exchange, 400, "{\"success\":false,\"message\":\"title required\"}");
            return;
        }
        if (content.isEmpty()) {
            Utils.send(exchange, 400, "{\"success\":false,\"message\":\"content required\"}");
            return;
        }

        List<Announcement> all = loadAll();
        long id = DataStore.nextId(all);

        Announcement a = new Announcement(id, type, title, content, important, Utils.now(), u.getUsername(), expiresAt);
        append(a);
        // 同步写入MySQL
        try {
            com.hourai.prts.service.AnnouncementService announcementService = new com.hourai.prts.service.AnnouncementService();
            announcementService.addAnnouncement(a);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.send(exchange, 200, "{\"success\":true,\"id\":" + id + "}");
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static User resolveUser(HttpExchange exchange) {
        // token: "user-{id}" from LoginHandler
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null) return null;
        auth = auth.trim();
        if (auth.toLowerCase().startsWith("bearer ")) auth = auth.substring(7).trim();
        if (!auth.startsWith("user-")) return null;
        try {
            long uid = Long.parseLong(auth.substring("user-".length()));
            List<User> users = DataStore.loadUsers();
            for (User u : users) {
                if (u.getId() == uid) return u;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static synchronized List<Announcement> loadAll() throws IOException {
        Path f = DataStore.getAnnouncementsFile();
        if (!Files.exists(f)) return new ArrayList<>();
        List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
        List<Announcement> out = new ArrayList<>();
        for (String ln : lines) {
            if (ln == null || ln.trim().isEmpty()) continue;
            // new: id,type,title,content,important,createdAt,createdBy,expiresAt
            // old: id,title,content,important,createdAt,createdBy,expiresAt
            String[] p = ln.split(",", 8);
            if (p.length < 6) continue;
            try {
                long id = Long.parseLong(p[0]);
                if (p.length >= 8) {
                    String type = Utils.unescapeCsv(p[1]);
                    String title = Utils.unescapeCsv(p[2]);
                    String content = Utils.unescapeCsv(p[3]);
                    boolean important = Boolean.parseBoolean(p[4]);
                    String createdAt = Utils.unescapeCsv(p[5]);
                    String createdBy = Utils.unescapeCsv(p[6]);
                    String expiresAt = Utils.unescapeCsv(p[7]);
                    out.add(new Announcement(id, type, title, content, important, createdAt, createdBy, expiresAt));
                } else {
                    String title = Utils.unescapeCsv(p[1]);
                    String content = Utils.unescapeCsv(p[2]);
                    boolean important = Boolean.parseBoolean(p[3]);
                    String createdAt = Utils.unescapeCsv(p[4]);
                    String createdBy = Utils.unescapeCsv(p[5]);
                    String expiresAt = p.length >= 7 ? Utils.unescapeCsv(p[6]) : "";
                    out.add(new Announcement(id, "system", title, content, important, createdAt, createdBy, expiresAt));
                }
            } catch (Exception ignoreBadRow) {
            }
        }
        return out;
    }

    private static synchronized void append(Announcement a) throws IOException {
        Path f = DataStore.getAnnouncementsFile();
        String line = a.getId() + ","
                + Utils.csvEscape(a.getType()) + ","
                + Utils.csvEscape(a.getTitle()) + ","
                + Utils.csvEscape(a.getContent()) + ","
                + a.isImportant() + ","
                + Utils.csvEscape(a.getCreatedAt()) + ","
                + Utils.csvEscape(a.getCreatedBy()) + ","
                + Utils.csvEscape(a.getExpiresAt())
                + System.lineSeparator();
        Files.write(f, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static String toJson(Announcement a) {
        return "{" +
                "\"id\":" + a.getId() + "," +
                "\"type\":\"" + Utils.escapeJson(a.getType()) + "\"," +
                "\"title\":\"" + Utils.escapeJson(a.getTitle()) + "\"," +
                "\"content\":\"" + Utils.escapeJson(a.getContent()) + "\"," +
                "\"important\":" + a.isImportant() + "," +
                "\"createdAt\":\"" + Utils.escapeJson(a.getCreatedAt()) + "\"," +
                "\"createdBy\":\"" + Utils.escapeJson(a.getCreatedBy()) + "\"," +
                "\"expiresAt\":\"" + Utils.escapeJson(a.getExpiresAt() == null ? "" : a.getExpiresAt()) + "\"" +
                "}";
    }
}
