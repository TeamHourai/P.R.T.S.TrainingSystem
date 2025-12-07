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

        server.createContext("/register", new CorsFilter(new RegisterHandler()));
        server.createContext("/login", new CorsFilter(new LoginHandler()));
        server.createContext("/questions", new CorsFilter(new QuestionsHandler()));
        server.createContext("/exam/paper", new CorsFilter(new ExamPaperHandler()));
        server.createContext("/exam/submit", new CorsFilter(new ExamSubmitHandler()));
        server.createContext("/user", new CorsFilter(new UserHandler())); // handles /user/{id}/wrong
        server.createContext("/ping", new CorsFilter(new PingHandler()));

        server.setExecutor(null);
        System.out.println("Server started at http://localhost:8888");
        server.start();
    }
}