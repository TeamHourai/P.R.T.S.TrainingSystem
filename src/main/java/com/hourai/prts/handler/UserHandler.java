package com.hourai.prts.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hourai.prts.utils.Utils;
import com.hourai.prts.entity.*;
import com.hourai.prts.service.WrongQuestionService;
import java.io.IOException;
import java.util.List;

/*
  GET /user/{id}/wrong  -> return user's wrong questions
*/
public class UserHandler implements HttpHandler {
    private final WrongQuestionService wrongQuestionService = new WrongQuestionService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange,405,"{\"error\":\"GET required\"}");
            return;
        }
        String path = exchange.getRequestURI().getPath(); // /user/2/wrong
        String[] segs = path.split("/");
        if (segs.length < 4) { Utils.send(exchange,400,"{\"error\":\"bad path\"}"); return; }
        try {
            long userId = Long.parseLong(segs[2]);
            String action = segs[3];
            if (!"wrong".equals(action)) { Utils.send(exchange,404,"{\"error\":\"unknown action\"}"); return; }
            List<Question> qs = wrongQuestionService.getVisibleWrongQuestions(userId);
            Utils.send(exchange,200, Utils.questionsToJson(qs));
        } catch (NumberFormatException nfe) {
            Utils.send(exchange,400,"{\"error\":\"invalid user id\"}");
        }
    }
}