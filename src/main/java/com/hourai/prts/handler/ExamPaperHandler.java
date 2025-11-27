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
*/
public class ExamPaperHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange,405,"{\"error\":\"GET required\"}");
            return;
        }
        URI uri = exchange.getRequestURI();
        String q = uri.getQuery();
        int count = 10;
        if (q != null) {
            Map<String,String> mp = Utils.parseQuery(q);
            if (mp.containsKey("count")) {
                try { count = Integer.parseInt(mp.get("count")); } catch (Exception ignored) {}
            }
        }
        List<Question> all = DataStore.loadQuestions();
        Collections.shuffle(all);
        List<Question> sel = all.size() <= count ? all : all.subList(0, count);
        Utils.send(exchange,200, Utils.questionsToJson(sel));
    }
}