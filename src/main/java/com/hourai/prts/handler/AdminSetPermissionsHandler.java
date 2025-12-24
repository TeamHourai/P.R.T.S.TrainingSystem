package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 管理员设置用户权限的接口处理器。
 * POST /api/v1/admin/user/permission
 * 请求体: application/x-www-form-urlencoded
 * 参数:
 *   actor_id   - 发起操作的用户 id（必须为管理员）
 *   target_id  - 要修改权限的目标用户 id
 *   make_admin - true/false 或 1/0
 *
 * 权限规则:
 *  - 任何管理员都可以把非管理员设为管理员
 *  - 只有 actor_id == 1 的管理员可以把管理员设为非管理员
 */
public class AdminSetPermissionsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method Not Allowed\"}");
            return;
        }

        // 解析 form body
        String body = readAll(exchange.getRequestBody());
        Map<String, String> params = parseForm(body);

        String actorS = params.get("actor_id");
        String targetS = params.get("target_id");
        String makeAdminS = params.get("make_admin");

        if (actorS == null || targetS == null || makeAdminS == null) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"missing parameters\"}");
            return;
        }
        if (!actorS.matches("\\d+") || !targetS.matches("\\d+")) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"invalid id\"}");
            return;
        }

        int actorId = Integer.parseInt(actorS);
        int targetId = Integer.parseInt(targetS);
        boolean makeAdmin = makeAdminS.equals("1") || makeAdminS.equalsIgnoreCase("true");

        try {
            // 校验 actor 是否为管理员
            boolean actorIsAdmin = DataStore.isUserAdmin(actorId);
            if (!actorIsAdmin) {
                sendJson(exchange, 403, "{\"success\":false,\"message\":\"actor is not admin\"}");
                return;
            }

            // 检查目标当前是否为管理员
            boolean targetIsAdmin = DataStore.isUserAdmin(targetId);

            // 非管理员升为管理员：允许（只要 actor 是管理员）
            if (!targetIsAdmin && makeAdmin) {
                boolean ok = DataStore.setUserAdmin(targetId, true);
                if (ok) {
                    sendJson(exchange, 200, "{\"success\":true,\"message\":\"promoted to admin\"}");
                } else {
                    sendJson(exchange, 500, "{\"success\":false,\"message\":\"failed to promote\"}");
                }
                return;
            }

            // 把管理员降为非管理员：仅允许 actorId == 1
            if (targetIsAdmin && !makeAdmin) {
                if (actorId != 1) {
                    sendJson(exchange, 403, "{\"success\":false,\"message\":\"只有超级管理员 (id=1) 才能降级其他管理员\"}");
                    return;
                }
                boolean ok = DataStore.setUserAdmin(targetId, false);
                if (ok) {
                    sendJson(exchange, 200, "{\"success\":true,\"message\":\"demoted from admin\"}");
                } else {
                    sendJson(exchange, 500, "{\"success\":false,\"message\":\"failed to demote\"}");
                }
                return;
            }

            // 其他情况（比如目标已经是所需状态）
            sendJson(exchange, 200, "{\"success\":false,\"message\":\"no change needed\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"internal error\"}");
        }
    }

    private static String readAll(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static Map<String, String> parseForm(String body) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isEmpty()) return map;
        String[] pairs = body.split("&");
        for (String p : pairs) {
            String[] kv = p.split("=", 2);
            String k = URLDecoder.decode(kv[0], "UTF-8");
            String v = kv.length > 1 ? URLDecoder.decode(kv[1], "UTF-8") : "";
            map.put(k, v);
        }
        return map;
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        com.hourai.prts.utils.Utils.send(exchange, status, json);
    }
}
