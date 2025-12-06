package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hourai.prts.utils.Utils;
import com.hourai.prts.entity.*;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/*
  GET /user/{id}/wrong  -> return user's wrong questions
*/
public class UserHandler implements HttpHandler {
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
            List<UserAnswer> uas = DataStore.loadUserAnswers().stream().filter(a->a.getUserId() == userId && !a.isCorrect()).collect(Collectors.toList());
            Set<Long> qids = uas.stream().map(a->a.getQuestionId()).collect(Collectors.toCollection(LinkedHashSet::new));
            List<Question> qs = DataStore.loadQuestions().stream().filter(q->qids.contains(q.getId())).collect(Collectors.toList());
            Utils.send(exchange,200, Utils.questionsToJson(qs));
        } catch (NumberFormatException nfe) {
            Utils.send(exchange,400,"{\"error\":\"invalid user id\"}");
        }
    }
}