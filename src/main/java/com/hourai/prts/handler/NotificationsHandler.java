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

    private static final Path STATE_FILE = Optional.ofNullable(DataStore.getAnnouncementsFile().getParent())
            .orElse(Path.of("data"))
            .resolve("notifications_state.csv");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        ensureStateFile();

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

    private static void ensureStateFile() throws IOException {
        if (!Files.exists(STATE_FILE)) {
            if (!Files.exists(STATE_FILE.getParent())) {
                Files.createDirectories(STATE_FILE.getParent());
            }
            Files.createFile(STATE_FILE);
        }
    }

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
            List<User> users = DataStore.loadUsers();
            for (User u : users) {
                if (u.getId() == uid) return u;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static synchronized List<Announcement> loadAnnouncements() throws IOException {
        Path f = DataStore.getAnnouncementsFile();
        if (!Files.exists(f)) return new ArrayList<>();
        List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
        List<Announcement> out = new ArrayList<>();
        for (String ln : lines) {
            if (ln == null || ln.trim().isEmpty()) continue;
            // Split by comma, but we need to handle the case where content might contain commas if not properly escaped.
            // However, Utils.unescapeCsv assumes simple splitting.
            // The issue is that p.split(",", 7) limits the split to 7 parts, which merges the last parts if there are more commas.
            // But if we have 8 columns (new format), split(",", 7) will merge the last two columns into one string.
            // We should split by -1 to get all parts, or at least 8.
            String[] p = ln.split(",", -1);
            if (p.length < 6) continue;
            try {
                long id = Long.parseLong(p[0]);
                // CSV format: id,type,title,content,important,createdAt,createdBy,expiresAt

                String type = "system";
                String title;
                String content;
                boolean important;
                String createdAt;
                String createdBy;
                String expiresAt = "";

                // Check if the second column looks like a type (short, lowercase letters)
                // or if we have enough columns for the new format.
                // The example CSV row is: 1,reward,阿米娅生日和Adonis生日,记得上线明日方舟领取200合成玉补偿,true,2025-12-23 22:44:31,二狗子,2025-12-23T16:00:00.000Z
                // This has 8 parts.

                if (p.length >= 8) {
                    // New format with type
                    type = Utils.unescapeCsv(p[1]);
                    title = Utils.unescapeCsv(p[2]);
                    content = Utils.unescapeCsv(p[3]);
                    important = Boolean.parseBoolean(p[4]);
                    createdAt = Utils.unescapeCsv(p[5]);
                    createdBy = Utils.unescapeCsv(p[6]);
                    expiresAt = Utils.unescapeCsv(p[7]);
                } else {
                    // Legacy format (no type column)
                    // id,title,content,important,createdAt,createdBy,expiresAt
                    title = Utils.unescapeCsv(p[1]);
                    content = Utils.unescapeCsv(p[2]);
                    important = Boolean.parseBoolean(p[3]);
                    createdAt = Utils.unescapeCsv(p[4]);
                    createdBy = Utils.unescapeCsv(p[5]);
                    if (p.length >= 7) {
                        expiresAt = Utils.unescapeCsv(p[6]);
                    }
                }

                out.add(new Announcement(id, type, title, content, important, createdAt, createdBy, expiresAt));
            } catch (Exception ignoreBadRow) {
            }
        }
        return out;
    }

    // state row: userId,notificationId,isRead,isHidden
    private static synchronized Map<Long, State> loadStateForUser(long userId) throws IOException {
        Map<Long, State> out = new HashMap<>();
        if (!Files.exists(STATE_FILE)) return out;
        List<String> lines = Files.readAllLines(STATE_FILE, StandardCharsets.UTF_8);
        for (String ln : lines) {
            if (ln == null || ln.trim().isEmpty()) continue;
            String[] p = ln.split(",", 4);
            if (p.length < 4) continue;
            try {
                long uid = Long.parseLong(p[0]);
                if (uid != userId) continue;
                long nid = Long.parseLong(p[1]);
                boolean read = Boolean.parseBoolean(p[2]);
                boolean hidden = Boolean.parseBoolean(p[3]);
                out.put(nid, new State(read, hidden));
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private static synchronized void upsertState(long userId, long notifId, Boolean read, Boolean hidden) throws IOException {
        List<String> lines = Files.exists(STATE_FILE) ? Files.readAllLines(STATE_FILE, StandardCharsets.UTF_8) : new ArrayList<>();
        boolean found = false;
        for (int i = 0; i < lines.size(); i++) {
            String ln = lines.get(i);
            if (ln == null || ln.trim().isEmpty()) continue;
            String[] p = ln.split(",", 4);
            if (p.length < 4) continue;
            try {
                long uid = Long.parseLong(p[0]);
                long nid = Long.parseLong(p[1]);
                if (uid == userId && nid == notifId) {
                    boolean curRead = Boolean.parseBoolean(p[2]);
                    boolean curHidden = Boolean.parseBoolean(p[3]);
                    boolean newRead = (read == null) ? curRead : read;
                    boolean newHidden = (hidden == null) ? curHidden : hidden;
                    lines.set(i, uid + "," + nid + "," + newRead + "," + newHidden);
                    found = true;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        if (!found) {
            boolean newRead = read != null && read;
            boolean newHidden = hidden != null && hidden;
            lines.add(userId + "," + notifId + "," + newRead + "," + newHidden);
        }
        Files.write(STATE_FILE, lines, StandardCharsets.UTF_8);
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
