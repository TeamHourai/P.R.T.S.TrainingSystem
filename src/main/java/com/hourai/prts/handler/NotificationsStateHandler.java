package com.hourai.prts.handler;

import com.hourai.prts.dao.NotificationsStateDao;
import com.hourai.prts.entity.NotificationsState;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;

public class NotificationsStateHandler implements HttpHandler {
    private final NotificationsStateDao dao = new NotificationsStateDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try {
            if ("GET".equalsIgnoreCase(method)) {
                // GET /notifications_state  查询所有
                List<NotificationsState> list = dao.selectAll();
                Utils.send(exchange, 200, Utils.toJson(list));
            } else if ("POST".equalsIgnoreCase(method)) {
                // POST /notifications_state  新增
                NotificationsState ns = Utils.parseJson(exchange, NotificationsState.class);
                dao.insert(ns);
                Utils.send(exchange, 200, "{\"message\":\"inserted\"}");
            } else if ("PUT".equalsIgnoreCase(method)) {
                // PUT /notifications_state/{id}/read  标记已读
                String[] parts = path.split("/");
                if (parts.length >= 4 && "read".equals(parts[3])) {
                    Long id = Long.parseLong(parts[2]);
                    dao.updateReadState(id, true, Utils.now());
                    Utils.send(exchange, 200, "{\"message\":\"marked as read\"}");
                } else {
                    Utils.send(exchange, 400, "{\"error\":\"invalid path\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                // DELETE /notifications_state/{id}  删除
                String[] parts = path.split("/");
                if (parts.length >= 3) {
                    Long id = Long.parseLong(parts[2]);
                    dao.deleteById(id);
                    Utils.send(exchange, 200, "{\"message\":\"deleted\"}");
                } else {
                    Utils.send(exchange, 400, "{\"error\":\"invalid path\"}");
                }
            } else {
                Utils.send(exchange, 405, "{\"error\":\"method not allowed\"}");
            }
        } catch (Exception e) {
            Utils.send(exchange, 500, "{\"error\":\"db error: " + Utils.escapeJson(e.getMessage()) + "\"}");
        }
    }
}
