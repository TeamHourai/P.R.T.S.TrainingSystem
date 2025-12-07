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
*/
public class RegisterHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange,405,"{\"error\":\"POST required\"}");
            return;
        }
        Map<String,String> params = Utils.parseForm(exchange);
        String username = params.get("username");
        String password = params.get("password");
        if (username == null || password == null) {
            Utils.send(exchange,400,"{\"error\":\"username & password required\"}");
            return;
        }
        // 数据库独立存储
        com.hourai.prts.service.UserService userService = new com.hourai.prts.service.UserService();
        try {
            List<User> users = userService.getAllUsers();
            boolean exists = users.stream().anyMatch(u -> u.getUsername().equals(username));
            if (exists) {
                Utils.send(exchange,400,"{\"error\":\"username exists\"}");
                return;
            }
            // id由数据库自增
            User u = new User(null, username, password, false, Utils.now());
            userService.register(u);
            Utils.send(exchange,200,"{\"id\":"+u.getId()+",\"username\":\""+ Utils.escapeJson(username)+"\"}");
        } catch (Exception e) {
            Utils.send(exchange,500,"{\"error\":\"db error: "+Utils.escapeJson(e.getMessage())+"\"}");
        }
    }
}