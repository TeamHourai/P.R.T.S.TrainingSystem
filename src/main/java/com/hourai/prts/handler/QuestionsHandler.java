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
*/
public class QuestionsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange,405,"{\"error\":\"GET required\"}");
            return;
        }
        List<Question> qs = DataStore.loadQuestions();
        Utils.send(exchange,200, Utils.questionsToJson(qs));
    }
}