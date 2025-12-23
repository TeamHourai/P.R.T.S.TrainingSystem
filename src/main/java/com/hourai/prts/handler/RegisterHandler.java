package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.hourai.prts.entity.User;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/*
  POST /api/v1/auth/register

  Accepts:
    - application/x-www-form-urlencoded (username=...&password=...)
    - application/json ({"username":"...","password":"..."})
*/
public class RegisterHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange, 405, "{\"success\":false,\"message\":\"POST required\"}");
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

        String username = params.get("username");
        String password = params.get("password");
        username = username == null ? null : username.trim();
        password = password == null ? null : password.trim();

        // capture for lambdas
        final String uname = username;

        if (uname == null || uname.isEmpty() || password == null || password.isEmpty()) {
            Utils.send(exchange, 400, "{\"success\":false,\"message\":\"username & password required\"}");
            return;
        }

        List<User> users = DataStore.loadUsers();
        boolean exists = users.stream().anyMatch(u -> u.getUsername() != null && u.getUsername().equals(uname));
        if (exists) {
            Utils.send(exchange, 400, "{\"success\":false,\"message\":\"username exists\"}");
            return;
        }

        long id = DataStore.nextId(users);
        User u = new User(id, uname, password, false, Utils.now());
        DataStore.appendUser(u);

        Utils.send(exchange, 200,
                "{\"success\":true,\"id\":" + id + ",\"userId\":" + id + ",\"username\":\"" + Utils.escapeJson(uname) + "\"}");
    }
}