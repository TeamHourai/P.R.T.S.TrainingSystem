package com.hourai.prts;/*
  极简本地作业系统（多文件、无外部依赖）
  编译：
    javac *.java
  运行：
    java Main

  启动后（端口 8080）：
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        DataStore.ensureDataFiles();

        // Bind explicitly to localhost and ALWAYS use port 8080.
        // If 8080 is occupied, print the owning PID (best effort) and retry for a short period.
        HttpServer server = null;
        final int port = 8080;
        final InetSocketAddress addr = new InetSocketAddress("127.0.0.1", port);
        int retries = 10;
        Exception last = null;
        while (retries-- > 0) {
            try {
                server = HttpServer.create(addr, 0);
                last = null;
                break;
            } catch (Exception e) {
                last = e;
                System.err.println("[ERROR] Failed to bind http://127.0.0.1:" + port + " (" + e.getClass().getSimpleName() + ")");
                printPortOwner(port);
                Thread.sleep(500);
            }
        }
        if (server == null) {
            throw last;
        }

        // 用 HandlerRegistry.getWrapped(...) 统一获取已包裹 CORS 的 handler
        server.createContext("/register", HandlerRegistry.getWrapped("register"));
        server.createContext("/login", HandlerRegistry.getWrapped("login"));
        server.createContext("/questions", HandlerRegistry.getWrapped("questions"));
        server.createContext("/exam/paper", HandlerRegistry.getWrapped("exam_paper"));
        server.createContext("/exam/submit", HandlerRegistry.getWrapped("exam_submit"));
        server.createContext("/exam/history", HandlerRegistry.getWrapped("exam_history"));
        server.createContext("/user", HandlerRegistry.getWrapped("user")); // handles /user/{id}/wrong
        server.createContext("/ping", HandlerRegistry.getWrapped("ping"));
        server.createContext("/stats", HandlerRegistry.getWrapped("stats"));

        // ===== 为兼容前端，增加带 /api 和 /api/v1 前缀的路由 =====
        server.createContext("/api/auth/register", HandlerRegistry.getWrapped("register"));
        server.createContext("/api/auth/login", HandlerRegistry.getWrapped("login"));
        server.createContext("/api/auth/profile", HandlerRegistry.getWrapped("auth_profile"));
        server.createContext("/api/questions", HandlerRegistry.getWrapped("questions"));
        server.createContext("/api/exam/paper", HandlerRegistry.getWrapped("exam_paper"));
        server.createContext("/api/exam/submit", HandlerRegistry.getWrapped("exam_submit"));
        server.createContext("/api/exam/history", HandlerRegistry.getWrapped("exam_history"));
        server.createContext("/api/answers/wrong", HandlerRegistry.getWrapped("answers_wrong"));
        server.createContext("/api/user", HandlerRegistry.getWrapped("user"));
        server.createContext("/api/ping", HandlerRegistry.getWrapped("ping"));
        server.createContext("/api/stats", HandlerRegistry.getWrapped("stats"));

        server.createContext("/api/v1/auth/register", HandlerRegistry.getWrapped("register"));
        server.createContext("/api/v1/auth/login", HandlerRegistry.getWrapped("login"));
        server.createContext("/api/v1/auth/profile", HandlerRegistry.getWrapped("auth_profile"));
        server.createContext("/api/v1/questions", HandlerRegistry.getWrapped("questions"));
        server.createContext("/api/v1/exam/paper", HandlerRegistry.getWrapped("exam_paper"));
        server.createContext("/api/v1/exam/submit", HandlerRegistry.getWrapped("exam_submit"));
        server.createContext("/api/v1/exam/history", HandlerRegistry.getWrapped("exam_history"));
        server.createContext("/api/v1/answers/wrong", HandlerRegistry.getWrapped("answers_wrong"));
        server.createContext("/api/v1/user", HandlerRegistry.getWrapped("user"));
        server.createContext("/api/v1/ping", HandlerRegistry.getWrapped("ping"));
        server.createContext("/api/v1/stats", HandlerRegistry.getWrapped("stats"));

        server.createContext("/api/v1/training/questions", HandlerRegistry.getWrapped("questions"));

        // 支持旧版管理接口的批量删除
        server.createContext("/admin/questions/batch-delete", HandlerRegistry.getWrapped("admin_questions"));
        // 关键词列表
        server.createContext("/api/v1/keywords", HandlerRegistry.getWrapped("keywords"));
        server.createContext("/keywords", HandlerRegistry.getWrapped("keywords"));

        // ======================================================

        server.setExecutor(null);
        System.out.println("Server started at http://localhost:8080");
        server.start();
    }

    /**
     * Best-effort diagnostics on Windows: runs `netstat -ano` and prints any lines containing :port,
     * so the user can see which PID is blocking the port.
     */
    private static void printPortOwner(int port) {
        try {
            Process p = new ProcessBuilder("cmd", "/c", "netstat -ano | findstr :" + port).redirectErrorStream(true).start();
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) lines.add(line);
                }
            }
            p.waitFor();
            if (lines.isEmpty()) {
                System.err.println("[INFO] netstat did not show any active LISTENING entry for :" + port + ". It may be an excluded/reserved port or a race.");
            } else {
                System.err.println("[INFO] netstat lines for :" + port + ":");
                for (String l : lines) {
                    System.err.println("  " + l);
                }
                System.err.println("[INFO] If a PID is shown at the end of the LISTENING line, stop it to free the port:");
                System.err.println("       Task Manager -> Details -> PID, or run: taskkill /PID <pid> /F");
            }
        } catch (Exception ignored) {
            // ignore diagnostics failures
        }
    }
}