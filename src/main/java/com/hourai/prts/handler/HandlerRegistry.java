package com.hourai.prts.handler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.hourai.prts.utils.Utils;
import com.hourai.prts.CorsFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
  HandlerRegistry: 集中管理 handler 实例，便于统一注册与扩展。
  - HANDLERS 存储 key -> HttpHandler
  - get(name) 返回原始 handler（可能为 null）
  - getWrapped(name) 返回带 CorsFilter 包裹的 handler（若找不到返回占位 404 handler）
  - register(name, handler) 可在运行期注册新的 handler
*/
public final class HandlerRegistry {
    private static final Map<String, HttpHandler> HANDLERS = new ConcurrentHashMap<>();

    static {
        // 初始化常用 handler 实例（与现有 Handler 类一一对应）
        HANDLERS.put("register", new RegisterHandler());
        HANDLERS.put("login", new LoginHandler());
        HANDLERS.put("logout", new LogoutHandler());
        HANDLERS.put("auth_profile", new AuthProfileHandler());
        HANDLERS.put("answers_wrong", new WrongAnswersHandler());
        HANDLERS.put("questions", new QuestionsHandler());
        HANDLERS.put("exam_paper", new ExamPaperHandler());
        HANDLERS.put("exam_submit", new ExamSubmitHandler());
        HANDLERS.put("exam_history", new ExamHistoryHandler());
        HANDLERS.put("user", new UserHandler());
        HANDLERS.put("ping", new PingHandler());
        // 新增：admin questions batch delete handler
        HANDLERS.put("admin_questions", new AdminQuestionsHandler());
        // 关键词列表
        HANDLERS.put("keywords", new KeywordsHandler());
        // 统计接口
        HANDLERS.put("stats", new StatsHandler());
        // 系统公告（管理员发布/用户查看）
        HANDLERS.put("announcements", new AnnouncementsHandler());

        // 通知中心（由公告映射而来，支持已读/隐藏状态）
        HANDLERS.put("notifications", new NotificationsHandler());

        // 通知状态表（notifications_state）数据库操作接口
        HANDLERS.put("notifications_state", new NotificationsStateHandler());

        // 新增：管理员设置用户权限
        HANDLERS.put("admin_set_permissions", new AdminSetPermissionsHandler());

        // 新增：管理员获取用户列表
        HANDLERS.put("admin_user_list", new AdminUserListHandler());
    }

    private HandlerRegistry() { /* no instantiation */ }

    public static HttpHandler get(String name) {
        return HANDLERS.get(name);
    }

    // 返回已包裹 CorsFilter 的 handler；若不存在，则返回一个 404 占位 handler
    public static HttpHandler getWrapped(final String name) {
        final HttpHandler h = get(name);
        if (h == null) {
            return new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String msg = "{\"error\":\"handler not found: " + Utils.escapeJson(name) + "\"}";
                    Utils.send(exchange, 404, msg);
                }
            };
        }
        return new CorsFilter(h);
    }

    // 允许在运行时注册或覆盖 handler
    public static void register(String name, HttpHandler handler) {
        if (name == null || handler == null) return;
        HANDLERS.put(name, handler);
    }
}
