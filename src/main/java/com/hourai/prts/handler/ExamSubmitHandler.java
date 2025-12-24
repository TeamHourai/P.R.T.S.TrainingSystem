package com.hourai.prts.handler;

import com.hourai.prts.service.UserAnswerService;
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
            com.hourai.prts.service.QuestionService questionService = new com.hourai.prts.service.QuestionService();
            List<Question> questions = questionService.getAllQuestions();
            Map<Long, Question> qm = questions.stream().collect(java.util.stream.Collectors.toMap(Question::getId, q -> q));

            int score = 0;
            UserAnswerService userAnswerService = new UserAnswerService();

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

                UserAnswer ua = new UserAnswer();
                ua.setUserId(userId);
                ua.setQuestionId(qid);
                ua.setSelectedAnswer(String.valueOf(sel));
                ua.setCorrect(correct);
                ua.setAnswerTime(null);
                try {
                    ua.setCreatedAt(java.sql.Timestamp.valueOf(Utils.now()));
                } catch (Exception ignored) {}
                try {
                    userAnswerService.addUserAnswer(ua);
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }

            com.hourai.prts.service.ExamRecordService examRecordService = new com.hourai.prts.service.ExamRecordService();
            ExamRecord rec = new ExamRecord();
            rec.setUserId(userId);
            rec.setScore(java.math.BigDecimal.valueOf(score));
            rec.setExamName("auto");
            rec.setTotalQuestions(0);
            rec.setCorrectCount(score);
            try {
                examRecordService.addExamRecord(rec);
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
            Long erId = rec.getId();
            Utils.send(exchange, 200, "{\"examId\":" + (erId == null ? -1 : erId) + ",\"score\":" + score + "}");
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