package com.hourai.prts;/*
  极简本地作业系统（多文件、无外部依赖）
  编译：
    javac *.java
  运行：
    java Main

  启动后（端口 8888）：
    POST /register      username=xx&password=yy
    POST /login         username=xx&password=yy
    GET  /questions
    GET  /exam/paper?count=5
    POST /exam/submit   userId=2&answers=1:2,3:1
    GET  /user/2/wrong
*/
import com.hourai.prts.data.DataStore;
import com.hourai.prts.handler.*;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        DataStore.ensureDataFiles();

        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);

        // 用 HandlerRegistry.getWrapped(...) 统一获取已包裹 CORS 的 handler
        server.createContext("/register", HandlerRegistry.getWrapped("register"));
        server.createContext("/login", HandlerRegistry.getWrapped("login"));
        server.createContext("/questions", HandlerRegistry.getWrapped("questions"));
        server.createContext("/exam/paper", HandlerRegistry.getWrapped("exam_paper"));
        server.createContext("/exam/submit", HandlerRegistry.getWrapped("exam_submit"));
        server.createContext("/user", HandlerRegistry.getWrapped("user")); // handles /user/{id}/wrong
        server.createContext("/ping", HandlerRegistry.getWrapped("ping"));

        // ===== 为兼容前端，增加带 /api 和 /api/v1 前缀的路由 =====
        server.createContext("/api/register", HandlerRegistry.getWrapped("register"));
        server.createContext("/api/login", HandlerRegistry.getWrapped("login"));
        server.createContext("/api/questions", HandlerRegistry.getWrapped("questions"));
        server.createContext("/api/exam/paper", HandlerRegistry.getWrapped("exam_paper"));
        server.createContext("/api/exam/submit", HandlerRegistry.getWrapped("exam_submit"));
        server.createContext("/api/user", new ApiHandler()); // handles /api/user
        server.createContext("/api/ping", HandlerRegistry.getWrapped("ping"));

        server.createContext("/api/v1/register", HandlerRegistry.getWrapped("register"));
        server.createContext("/api/v1/login", HandlerRegistry.getWrapped("login"));
        server.createContext("/api/v1/questions", HandlerRegistry.getWrapped("questions"));
        server.createContext("/api/v1/exam/paper", HandlerRegistry.getWrapped("exam_paper"));
        server.createContext("/api/v1/exam/submit", HandlerRegistry.getWrapped("exam_submit"));
        server.createContext("/api/v1/user", HandlerRegistry.getWrapped("user"));
        server.createContext("/api/v1/ping", HandlerRegistry.getWrapped("ping"));
        // 注册统一数据导入接口
        server.createContext("/api/question", new ApiHandler());
        server.createContext("/api/exam_record", new ApiHandler());
        server.createContext("/api/user_answer", new ApiHandler());
        // ======================================================

        server.setExecutor(null);
        System.out.println("Server started at http://localhost:8888");
        server.start();
    }
}