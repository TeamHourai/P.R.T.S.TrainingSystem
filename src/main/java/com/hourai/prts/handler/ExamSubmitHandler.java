package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hourai.prts.utils.Utils;
import com.hourai.prts.entity.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/*
  POST /exam/submit
  body: userId=...&answers=1:2,3:1
*/
public class ExamSubmitHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange,405,"{\"error\":\"POST required\"}");
            return;
        }
        Map<String,String> params = Utils.parseForm(exchange);
        String userIdS = params.get("userId");
        String answersS = params.get("answers");
        if (userIdS == null || answersS == null) {
            Utils.send(exchange,400,"{\"error\":\"userId & answers required\"}");
            return;
        }
        long userId;
        try { userId = Long.parseLong(userIdS); } catch (Exception e) { Utils.send(exchange,400,"{\"error\":\"userId invalid\"}"); return; }
        Map<Long,Integer> answers = Utils.parseAnswers(answersS);
        List<Question> questions = DataStore.loadQuestions();
        Map<Long, Question> qm = questions.stream().collect(java.util.stream.Collectors.toMap(q->q.getId(), q->q));
        int score = 0;
        List<UserAnswer> ualist = DataStore.loadUserAnswers();
        long uaNext = DataStore.nextId(ualist);
        for (Map.Entry<Long,Integer> e : answers.entrySet()){
            Long qid = e.getKey(); Integer sel = e.getValue();
            Question qObj = qm.get(qid);
            boolean correct = qObj != null && qObj.getAnswer().equals(String.valueOf(sel));
            if (correct) score += 1;
            UserAnswer ua = new UserAnswer(uaNext++, userId, qid, "normal", correct, sel, Utils.now());
            DataStore.appendUserAnswer(ua);
        }
        List<ExamRecord> ers = DataStore.loadExamRecords();
        long erId = DataStore.nextId(ers);
        ExamRecord rec = new ExamRecord(erId, userId, score, Utils.now());
        DataStore.appendExamRecord(rec);
        Utils.send(exchange,200,"{\"examId\":"+erId+",\"score\":"+score+"}");
    }
}