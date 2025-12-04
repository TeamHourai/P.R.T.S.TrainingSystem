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
  用于提交考试答案，返回考试记录ID和得分
*/
public class ExamSubmitHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
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

        // 解析答案
        Map<Long, Integer> answers = Utils.parseAnswers(answersS);
        // 保存用户答案
        List<Question> questions = DataStore.loadQuestions();
        // 构建题目ID到题目对象的映射
        Map<Long, Question> qm = questions.stream().collect(java.util.stream.Collectors.toMap(q -> q.id, q -> q));
        // 计算得分
        int score = 0;
        List<UserAnswer> ualist = DataStore.loadUserAnswers();
        long uaNext = DataStore.nextId(ualist);
        // 遍历提交的答案
        for (Map.Entry<Long, Integer> e : answers.entrySet()) {
            Long qid = e.getKey();
            Integer sel = e.getValue();
            Question qObj = qm.get(qid);
            boolean correct = qObj != null && qObj.answer == sel;
            // 如果答对则加分，一题4分
            if (correct) score += 4;
            /* 【需要后续修改！！！】题目类型为"dispatch";"deployment";"efficiency";"analysis";"environment".
             * 不应该存在"normal"类型。
             * 后续需要根据题目类型进行不同处理，目前先统一保存为"normal"。
             */
            UserAnswer ua = new UserAnswer(uaNext++, userId, qid, "normal", correct, sel, Utils.now());
            DataStore.appendUserAnswer(ua);
        }
        // 保存考试记录
        List<ExamRecord> ers = DataStore.loadExamRecords();
        long erId = DataStore.nextId(ers);
        ExamRecord rec = new ExamRecord(erId, userId, score, Utils.now());
        DataStore.appendExamRecord(rec);
        Utils.send(exchange, 200, "{\"examId\":" + erId + ",\"score\":" + score + "}");
    }
}