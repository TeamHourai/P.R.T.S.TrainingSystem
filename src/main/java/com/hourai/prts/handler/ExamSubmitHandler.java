package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hourai.prts.utils.Utils;
import com.hourai.prts.entity.*;
import com.hourai.prts.service.WrongQuestionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/*
  POST /exam/submit
  body: userId=...&answers=1:2,3:1
*/
public class ExamSubmitHandler implements HttpHandler {
    private final WrongQuestionService wrongQuestionService = new WrongQuestionService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Utils.send(exchange, 405, "{\"error\":\"POST required\"}");
                return;
            }

            Map<String, String> params = Utils.parseForm(exchange);
            String userIdS = params.get("userId");
            String answersS = params.get("answers");

            if (userIdS == null || answersS == null) {
                Utils.send(exchange, 400, "{\"error\":\"userId & answers required\"}");
                return;
            }

            long userId;
            try {
                userId = Long.parseLong(userIdS);
            } catch (Exception e) {
                Utils.send(exchange, 400, "{\"error\":\"userId invalid\"}");
                return;
            }

            Map<Long, Integer> answers = Utils.parseAnswers(answersS);
            List<Question> questions = DataStore.loadQuestions();
            Map<Long, Question> qm = questions.stream().collect(java.util.stream.Collectors.toMap(Question::getId, q -> q));

            int score = 0;
            List<UserAnswer> ualist = DataStore.loadUserAnswers();
            long uaNext = DataStore.nextId(ualist);

            for (Map.Entry<Long, Integer> e : answers.entrySet()) {
                Long qid = e.getKey();
                Integer sel = e.getValue();
                Question qObj = qm.get(qid);
                // Question.answer is stored as String of option index
                boolean correct = qObj != null && qObj.getAnswer() != null && qObj.getAnswer().equals(String.valueOf(sel));
                if (correct) score += 1;

                // Rule: if user answers wrong again, unhide it so it can show in wrong list.
                if (!correct) {
                    try {
                        wrongQuestionService.unhideIfHidden(userId, qid);
                    } catch (Exception ignored) {
                        // never break exam submit for visibility bookkeeping
                    }
                }

                UserAnswer ua = new UserAnswer(uaNext++, userId, qid, "normal", correct, sel, Utils.now());
                DataStore.appendUserAnswer(ua);
                // 同步写入MySQL
                try {
                    com.hourai.prts.service.UserAnswerService userAnswerService = new com.hourai.prts.service.UserAnswerService();
                    userAnswerService.addUserAnswer(ua);
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }

            List<ExamRecord> ers = DataStore.loadExamRecords();
            long erId = DataStore.nextId(ers);
            ExamRecord rec = new ExamRecord(erId, userId, score, Utils.now());
            DataStore.appendExamRecord(rec);
            // 同步写入MySQL
            try {
                com.hourai.prts.service.ExamRecordService examRecordService = new com.hourai.prts.service.ExamRecordService();
                examRecordService.addExamRecord(rec);
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
            Utils.send(exchange, 200, "{\"examId\":" + erId + ",\"score\":" + score + "}");
        } catch (Exception ex) {
            ex.printStackTrace();
            // Never drop the connection: always respond
            try {
                Utils.send(exchange, 500, "{\"error\":\"internal server error\"}");
            } catch (Exception ignored) {
                // ignore secondary failures
            }
        }
    }
}