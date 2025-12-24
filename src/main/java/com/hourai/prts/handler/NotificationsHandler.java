package com.hourai.prts.handler;

import com.hourai.prts.entity.Announcement;
import com.hourai.prts.entity.User;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.*;

/**
 * Notifications API used by the "通知中心" on index.html.
 *
 * This project previously only had a frontend notificationApi stub.
 * We implement a lightweight server-side notifications source backed by announcements.csv.
 *
 * Endpoints:
 *   GET    /notifications?unreadOnly=false&page=1&size=20&type=
 *   PUT    /notifications/{id}/read
 *   PUT    /notifications/read-all
 *   DELETE /notifications/{id}
 *   DELETE /notifications
 *   GET    /notifications/unread-count
 *
 * Important:
 * - Deleting/marking read does NOT delete announcements.csv.
 * - Per-user read/hidden state is stored in notifications_state.csv.
 */
public class NotificationsHandler implements HttpHandler {

    // Notification states are persisted in DB via NotificationStateService

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // state persisted in DB; no local file required
        if ("GET".equalsIgnoreCase(method)) {
            if (path.endsWith("/unread-count")) {
                handleUnreadCount(exchange);
                return;
            }
            handleList(exchange);
            return;
        }

        if ("PUT".equalsIgnoreCase(method)) {
            if (path.endsWith("/read-all")) {
                handleReadAll(exchange);
                return;
            }
            if (path.matches(".*/notifications/\\d+/read$")) {
                handleMarkRead(exchange);
                return;
            }
            Utils.send(exchange, 404, "{\"success\":false,\"message\":\"not found\"}");
            return;
        }

        if ("DELETE".equalsIgnoreCase(method)) {
            if (path.matches(".*/notifications/\\d+$")) {
                handleDeleteOne(exchange);
                return;
            }
            if (path.endsWith("/notifications")) {
                handleDeleteAll(exchange);
                return;
            }
            Utils.send(exchange, 404, "{\"success\":false,\"message\":\"not found\"}");
            return;
        }

        Utils.send(exchange, 405, "{\"success\":false,\"message\":\"method not allowed\"}");
    }

    private void handleList(HttpExchange exchange) throws IOException {
        User user = resolveUser(exchange);
        if (user == null) {
            Utils.send(exchange, 401, "{\"success\":false,\"message\":\"login required\"}");
            return;
        }

        Map<String, String> qp = Utils.parseQuery(exchange.getRequestURI().getRawQuery());
        boolean unreadOnly = "true".equalsIgnoreCase(qp.getOrDefault("unreadOnly", "false"));
        // frontend uses `unread=true` (see app-methods4.js)
        if (!unreadOnly && "true".equalsIgnoreCase(qp.getOrDefault("unread", "false"))) {
            unreadOnly = true;
        }
        int page = parseInt(qp.get("page"), 1);
        int size = parseInt(qp.get("size"), 20);
        String type = qp.get("type");

        // Base items: announcements -> notifications
        List<Announcement> announcements = loadAnnouncements();
        announcements.sort((a,b) -> Long.compare(b.getId(), a.getId()));

        Map<Long, State> stateMap = loadStateForUser(user.getId());

        List<Map<String, Object>> items = new ArrayList<>();
        long unreadCount = 0;
        for (Announcement a : announcements) {
            State st = stateMap.getOrDefault(a.getId(), new State(false, false));
            if (st.hidden) continue;

            boolean isRead = st.read;
            if (!isRead) unreadCount++;

            if (unreadOnly && isRead) continue;
            if (type != null && !type.isEmpty() && !"system".equalsIgnoreCase(type)) {
                // only system notifications are supported for now
                continue;
            }

            Map<String, Object> n = new LinkedHashMap<>();
            n.put("id", a.getId());
            n.put("type", a.getType()); // Use actual type from Announcement
            n.put("title", a.getTitle());
            n.put("content", a.getContent());
            n.put("isRead", isRead);
            n.put("isImportant", a.isImportant());
            n.put("createdAt", a.getCreatedAt());
            items.add(n);
        }

        // paging
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        List<Map<String, Object>> pageItems = (from >= items.size()) ? Collections.emptyList() : items.subList(from, to);
        boolean hasMore = to < items.size();

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\":true,");
        sb.append("\"notifications\":[");
        for (int i = 0; i < pageItems.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(toJson(pageItems.get(i)));
        }
        sb.append("],");
        sb.append("\"unreadCount\":" + unreadCount + ",");
        sb.append("\"hasMore\":" + hasMore);
        sb.append("}");

        Utils.send(exchange, 200, sb.toString());
    }

    private void handleUnreadCount(HttpExchange exchange) throws IOException {
        User user = resolveUser(exchange);
        if (user == null) {
            Utils.send(exchange, 401, "{\"success\":false,\"message\":\"login required\"}");
            return;
        }
        List<Announcement> announcements = loadAnnouncements();
        Map<Long, State> stateMap = loadStateForUser(user.getId());

        long unread = 0;
        for (Announcement a : announcements) {
            State st = stateMap.getOrDefault(a.getId(), new State(false, false));
            if (st.hidden) continue;
            if (!st.read) unread++;
        }
        Utils.send(exchange, 200, "{\"success\":true,\"unreadCount\":" + unread + "}");
    }

    private void handleMarkRead(HttpExchange exchange) throws IOException {
        User user = resolveUser(exchange);
        if (user == null) {
            Utils.send(exchange, 401, "{\"success\":false,\"message\":\"login required\"}");
            return;
        }

        long id = extractId(exchange.getRequestURI().getPath());
        if (id <= 0) {
            Utils.send(exchange, 400, "{\"success\":false,\"message\":\"invalid id\"}");
            return;
        }

        upsertState(user.getId(), id, true, null);
        Utils.send(exchange, 200, "{\"success\":true}");
    }

    private void handleReadAll(HttpExchange exchange) throws IOException {
        User user = resolveUser(exchange);
        if (user == null) {
            Utils.send(exchange, 401, "{\"success\":false,\"message\":\"login required\"}");
            return;
        }

        for (Announcement a : loadAnnouncements()) {
            upsertState(user.getId(), a.getId(), true, null);
        }
        Utils.send(exchange, 200, "{\"success\":true}");
    }

    private void handleDeleteOne(HttpExchange exchange) throws IOException {
        User user = resolveUser(exchange);
        if (user == null) {
            Utils.send(exchange, 401, "{\"success\":false,\"message\":\"login required\"}");
            return;
        }

        long id = extractId(exchange.getRequestURI().getPath());
        if (id <= 0) {
            Utils.send(exchange, 400, "{\"success\":false,\"message\":\"invalid id\"}");
            return;
        }

        // hide for user (do not delete announcement)
        upsertState(user.getId(), id, null, true);
        Utils.send(exchange, 200, "{\"success\":true}");
    }

    private void handleDeleteAll(HttpExchange exchange) throws IOException {
        User user = resolveUser(exchange);
        if (user == null) {
            Utils.send(exchange, 401, "{\"success\":false,\"message\":\"login required\"}");
            return;
        }
        for (Announcement a : loadAnnouncements()) {
            upsertState(user.getId(), a.getId(), null, true);
        }
        Utils.send(exchange, 200, "{\"success\":true}");
    }

    // ===== helpers =====

    // no local state file

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private static long extractId(String path) {
        // /.../notifications/{id} or /.../notifications/{id}/read
        String[] seg = path.split("/");
        for (int i = seg.length - 1; i >= 0; i--) {
            if (seg[i] == null || seg[i].isEmpty()) continue;
            if ("read".equals(seg[i])) continue;
            if ("notifications".equals(seg[i])) continue;
            try { return Long.parseLong(seg[i]); } catch (Exception ignored) {}
        }
        return -1;
    }

    private static User resolveUser(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null) return null;
        auth = auth.trim();
        if (auth.toLowerCase().startsWith("bearer ")) auth = auth.substring(7).trim();
        if (!auth.startsWith("user-")) return null;
        try {
            long uid = Long.parseLong(auth.substring("user-".length()));
            com.hourai.prts.service.UserService userService = new com.hourai.prts.service.UserService();
            try {
                return userService.getUserById(uid);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static synchronized List<Announcement> loadAnnouncements() throws IOException {
        try {
            com.hourai.prts.service.AnnouncementService announcementService = new com.hourai.prts.service.AnnouncementService();
            return announcementService.getAllAnnouncements();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // state row: userId,notificationId,isRead,isHidden
    private static synchronized Map<Long, State> loadStateForUser(long userId) throws IOException {
        Map<Long, State> out = new HashMap<>();
        try {
            com.hourai.prts.service.NotificationStateService nsService = new com.hourai.prts.service.NotificationStateService();
            java.util.List<com.hourai.prts.entity.NotificationState> states = nsService.getStatesForUser(userId);
            for (com.hourai.prts.entity.NotificationState ns : states) {
                out.put(ns.getNotificationId(), new State(ns.isRead(), ns.isHidden()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // on DB error return empty map
        }
        return out;
    }

    private static synchronized void upsertState(long userId, long notifId, Boolean read, Boolean hidden) throws IOException {
        try {
            com.hourai.prts.service.NotificationStateService nsService = new com.hourai.prts.service.NotificationStateService();
            boolean r = read == null ? false : read;
            boolean h = hidden == null ? false : hidden;
            com.hourai.prts.entity.NotificationState ns = new com.hourai.prts.entity.NotificationState(userId, notifId, r, h);
            nsService.upsert(ns);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("failed to upsert notification state", e);
        }
    }

    private static String toJson(Map<String, Object> obj) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> e : obj.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(Utils.escapeJson(e.getKey())).append('"').append(':');
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v.toString());
            } else {
                sb.append('"').append(Utils.escapeJson(String.valueOf(v))).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private static class State {
        final boolean read;
        final boolean hidden;
        State(boolean read, boolean hidden) { this.read = read; this.hidden = hidden; }
    }
}
