package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hourai.prts.utils.Utils;
import com.hourai.prts.entity.Question;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
  GET /exam/paper?count=10
  随机抽取指定数量的题目作为试卷返回，默认10题
  每个难度每个类型随机抽取1题，共25题。
  【有余力则实现】根据用户历史答题情况，智能组卷，如掌握度最好的类型和难度少抽题，掌握度最差的多抽题等。
*/
public class ExamPaperHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange, 405, "{\"error\":\"GET required\"}");
            return;
        }

        List<Question> all = DataStore.loadQuestions();

        // Group questions: Type -> Difficulty -> List<Question>
        Map<Integer, Map<Integer, List<Question>>> matrix = new HashMap<>();
        for (Question q : all) {
            matrix.computeIfAbsent(q.getType(), k -> new HashMap<>())
                  .computeIfAbsent(q.getDifficulty(), k -> new ArrayList<>())
                  .add(q);
        }

        List<Question> examPaper = new ArrayList<>();
        Random rand = new Random();

        // Iterate Type 1-5, Difficulty 1-5
        for (int t = 1; t <= 5; t++) {
            for (int d = 1; d <= 5; d++) {
                List<Question> candidates = matrix.getOrDefault(t, Collections.emptyMap()).get(d);

                if (candidates != null && !candidates.isEmpty()) {
                    // Pick one random question for this slot
                    examPaper.add(candidates.get(rand.nextInt(candidates.size())));
                } else {
                    // Fallback: If no question for specific (Type, Diff), try to pick any from same Type
                    List<Question> sameType = new ArrayList<>();
                    if (matrix.containsKey(t)) {
                        for (List<Question> l : matrix.get(t).values()) {
                            sameType.addAll(l);
                        }
                    }

                    if (!sameType.isEmpty()) {
                        examPaper.add(sameType.get(rand.nextInt(sameType.size())));
                    } else {
                        // Last resort: pick any random question from all available
                        if (!all.isEmpty()) {
                            examPaper.add(all.get(rand.nextInt(all.size())));
                        }
                    }
                }
            }
        }

        Utils.send(exchange, 200, Utils.questionsToJson(examPaper));
    }
}