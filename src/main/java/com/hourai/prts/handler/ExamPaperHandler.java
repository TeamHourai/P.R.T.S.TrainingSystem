package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hourai.prts.utils.Utils;
import com.hourai.prts.entity.Question;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/*
  GET /exam/paper?count=10
  随机抽取指定数量的题目作为试卷返回，默认10题
  【需要后续修改！！！】预期功能应该是：每个难度每个类型随机抽取1题，共25题。
  【有余力则实现】根据用户历史答题情况，智能组卷，如掌握度最好的类型和难度少抽题，掌握度最差的多抽题等。
*/
public class ExamPaperHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange, 405, "{\"error\":\"GET required\"}");
            return;
        }
        URI uri = exchange.getRequestURI();
        String q = uri.getQuery();
        int count = 10;
        if (q != null) {
            Map<String, String> mp = Utils.parseQuery(q);
            if (mp.containsKey("count")) {
                try {
                    count = Integer.parseInt(mp.get("count"));
                } catch (Exception ignored) {
                }
            }
        }
        // 数据库独立存储
        com.hourai.prts.service.QuestionService qsvc = new com.hourai.prts.service.QuestionService();
        try {
            List<Question> all = qsvc.getAllQuestions();
            Collections.shuffle(all);
            List<Question> sel = all.size() <= count ? all : all.subList(0, count);
            Utils.send(exchange, 200, Utils.questionsToJson(sel));
        } catch (Exception e) {
            Utils.send(exchange,500,"{\"error\":\"db error: "+Utils.escapeJson(e.getMessage())+"\"}");
        }
    }
}