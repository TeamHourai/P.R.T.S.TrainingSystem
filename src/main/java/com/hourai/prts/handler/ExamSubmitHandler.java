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
        // 数据库独立存储
        com.hourai.prts.service.QuestionService qsvc = new com.hourai.prts.service.QuestionService();
        com.hourai.prts.service.UserAnswerService uasvc = new com.hourai.prts.service.UserAnswerService();
        com.hourai.prts.service.ExamRecordService ersvc = new com.hourai.prts.service.ExamRecordService();
        try {
            List<Question> questions = qsvc.getAllQuestions();
            Map<Long, Question> qm = questions.stream().collect(java.util.stream.Collectors.toMap(q->q.getId(), q->q));
            int score = 0;
            // id由数据库自增
            for (Map.Entry<Long,Integer> e : answers.entrySet()){
                Long qid = e.getKey(); Integer sel = e.getValue();
                Question qObj = qm.get(qid);
                boolean correct = qObj != null && qObj.getAnswer().equals(String.valueOf(sel));
                if (correct) score += 1;
                UserAnswer ua = new UserAnswer(null, userId, qid, "normal", correct, sel, Utils.now());
                uasvc.addUserAnswer(ua);
            }
            ExamRecord rec = new ExamRecord(null, userId, score, Utils.now());
            ersvc.addExamRecord(rec);
            Utils.send(exchange,200,"{\"examId\":"+rec.getId()+",\"score\":"+score+"}");
        } catch (Exception e) {
            Utils.send(exchange,500,"{\"error\":\"db error: "+Utils.escapeJson(e.getMessage())+"\"}");
        }
    }
}