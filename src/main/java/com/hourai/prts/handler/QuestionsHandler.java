package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.hourai.prts.utils.Utils;
import com.hourai.prts.entity.Question;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;

/*
  GET /questions
  获取所有题目列表
*/
public class QuestionsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange,405,"{\"error\":\"GET required\"}");
            return;
        }
        // 数据库独立存储
        com.hourai.prts.service.QuestionService qsvc = new com.hourai.prts.service.QuestionService();
        try {
            List<Question> qs = qsvc.getAllQuestions();
            Utils.send(exchange,200, Utils.questionsToJson(qs));
        } catch (Exception e) {
            Utils.send(exchange,500,"{\"error\":\"db error: "+Utils.escapeJson(e.getMessage())+"\"}");
        }
    }
}