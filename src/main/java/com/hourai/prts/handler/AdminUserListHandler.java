package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.hourai.prts.entity.User;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员获取用户列表接口
 * GET /api/v1/admin/users?q=keyword
 */
public class AdminUserListHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        String keyword = "";
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1 && "q".equals(pair[0])) {
                    keyword = pair[1].toLowerCase();
                }
            }
        }

        try {
            com.hourai.prts.service.UserService userService = new com.hourai.prts.service.UserService();
            List<User> allUsers = userService.getAllUsers();
            List<User> filteredUsers;

            if (keyword.isEmpty()) {
                filteredUsers = allUsers;
            } else {
                final String k = keyword;
                filteredUsers = allUsers.stream()
                    .filter(u -> String.valueOf(u.getId()).contains(k) ||
                                 u.getUsername().toLowerCase().contains(k))
                    .collect(Collectors.toList());
            }

            // Convert to JSON manually or use Utils helper if available for list
            // Since Utils.toJson(List) exists but is simple, we can use it.
            // However, we might want to mask passwords.

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < filteredUsers.size(); i++) {
                User u = filteredUsers.get(i);
                json.append("{")
                    .append("\"id\":").append(u.getId()).append(",")
                    .append("\"username\":\"").append(Utils.escapeJson(u.getUsername())).append("\",")
                    .append("\"isAdmin\":").append(u.isAdmin()).append(",")
                    .append("\"createdAt\":\"").append(u.getCreatedAt()).append("\"")
                    .append("}");
                if (i < filteredUsers.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            Utils.send(exchange, 200, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            Utils.send(exchange, 500, "{\"error\":\"Internal Server Error\"}");
        }
    }
}

