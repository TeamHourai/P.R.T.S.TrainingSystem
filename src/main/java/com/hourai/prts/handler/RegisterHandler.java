package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hourai.prts.utils.Utils;
import com.hourai.prts.entity.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/*
  POST /register  body: username=...&password=...
    用于用户注册
*/
public class RegisterHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange, 405, "{\"error\":\"POST required\"}");
            return;
        }
        Map<String, String> params = Utils.parseForm(exchange);
        String username = params.get("username");
        String password = params.get("password");
        // 用户名和密码不能为空
        if (username == null || password == null) {
            Utils.send(exchange, 400, "{\"error\":\"username & password required\"}");
            return;
        }
        List<User> users = DataStore.loadUsers();
        // 检查用户名是否已存在（不允许重名，不区分大小写）
        boolean exists = users.stream().anyMatch(u -> u.username.equals(username));
        if (exists) {
            Utils.send(exchange, 400, "{\"error\":\"username exists\"}");
            return;
        }
        // 自动分配ID
        long id = DataStore.nextId(users);
        // 创建用户，默认非管理员
        User u = new User(id, username, password, false, Utils.now());
        DataStore.appendUser(u);
        // 返回注册成功的用户信息
        Utils.send(exchange, 200, "{\"id\":" + id + ",\"username\":\"" + Utils.escapeJson(username) + "\"}");
    }
}