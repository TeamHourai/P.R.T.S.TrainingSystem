package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hourai.prts.utils.Utils;
import com.hourai.prts.entity.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
  POST /login  body: username & password
  用于用户登录验证
*/
public class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange, 405, "{\"error\":\"POST required\"}");
            return;
        }
        Map<String, String> params = Utils.parseForm(exchange);
        String username = params.get("username");
        String password = params.get("password");
        List<User> users = DataStore.loadUsers();
        // 查找匹配的用户
        Optional<User> ou = users.stream().filter(u -> u.username.equals(username) && u.password.equals(password)).findFirst();
        if (ou.isPresent()) {
            // 登录成功
            User u = ou.get();
            Utils.send(exchange, 200, "{\"id\":" + u.id + ",\"username\":\"" + Utils.escapeJson(u.username) + "\"}");
        } else {
            // 登录失败
            Utils.send(exchange, 401, "{\"error\":\"invalid credentials\"}");
        }
    }
}