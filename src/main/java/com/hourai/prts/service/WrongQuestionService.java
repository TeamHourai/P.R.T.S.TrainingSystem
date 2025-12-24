package com.hourai.prts.service;

import com.hourai.prts.dao.WrongQuestionVisibilityDao;
import com.hourai.prts.data.DataStore;
import com.hourai.prts.entity.Question;
import com.hourai.prts.entity.UserAnswer;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for wrong-question list.
 *
 * Source of truth for attempts remains data/user_answers.csv.
 * Visibility (hide/show) is stored in data/wrong_visibility.csv.
 */
public class WrongQuestionService {
    private final WrongQuestionVisibilityDao visibilityDao = new WrongQuestionVisibilityDao();

    /**
     * Returns wrong questions for a user, filtered by visibility (hidden=false).
     */
    public List<Question> getVisibleWrongQuestions(long userId) throws IOException {
        boolean dbOk = true;
        Set<Long> wrongIds = new LinkedHashSet<>();
        Set<Long> hiddenIds = new LinkedHashSet<>();
        List<Question> questions = new java.util.ArrayList<>();
        try {
            com.hourai.prts.service.UserAnswerService userAnswerService = new com.hourai.prts.service.UserAnswerService();
            com.hourai.prts.service.WrongQuestionVisibilityDbService dbService = new com.hourai.prts.service.WrongQuestionVisibilityDbService();
            // 查找所有答错的题目id
            List<com.hourai.prts.entity.UserAnswer> uas = userAnswerService.getAllUserAnswers();
            wrongIds = uas.stream()
                .filter(a -> a.getUserId() == userId && !a.isCorrect())
                .map(com.hourai.prts.entity.UserAnswer::getQuestionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
            // 查找所有隐藏的题目id
            java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", "root", "p.r.t.s.data115");
            java.sql.Statement st = conn.createStatement();
            java.sql.ResultSet rs = st.executeQuery("SELECT question_id FROM wrong_visibility WHERE user_id=" + userId + " AND visible=0");
            while (rs.next()) {
                hiddenIds.add(rs.getLong("question_id"));
            }
            rs.close(); st.close(); conn.close();
            // 查找所有题目
            com.hourai.prts.service.QuestionService questionService = new com.hourai.prts.service.QuestionService();
            questions = questionService.getAllQuestions();
        } catch (Exception e) {
            dbOk = false;
        }
        if (!dbOk) {
            wrongIds = computeWrongQuestionIds(userId);
            hiddenIds = visibilityDao.selectAll().stream()
                .filter(r -> r.getUserId() == userId && r.isHidden())
                .map(r -> r.getQuestionId())
                .collect(Collectors.toSet());
            questions = DataStore.loadQuestions();
        }
        wrongIds.removeAll(hiddenIds);
        Set<Long> finalWrongIds = wrongIds;
        return questions.stream().filter(q -> finalWrongIds.contains(q.getId())).collect(Collectors.toList());
    }

    /**
     * Hides a question from the wrong list for the user.
     */
    public void hideWrongQuestion(long userId, long questionId) throws IOException {
        boolean dbOk = true;
        try {
            com.hourai.prts.service.WrongQuestionVisibilityDbService dbService = new com.hourai.prts.service.WrongQuestionVisibilityDbService();
            com.hourai.prts.entity.WrongQuestionVisibility wqv = new com.hourai.prts.entity.WrongQuestionVisibility();
            wqv.setUserId(userId);
            wqv.setQuestionId(questionId);
            wqv.setHidden(true);
            wqv.setUpdatedAt(java.time.LocalDateTime.now().toString());
            dbService.upsert(wqv);
        } catch (Exception e) {
            dbOk = false;
        }
        if (!dbOk) {
            visibilityDao.upsert(userId, questionId, true);
        }
    }

    /**
     * If the question was previously hidden from the wrong list, unhide it.
     *
     * Rule: if the user deletes a wrong question but later answers it wrong again,
     * the hide mark should be cleared.
     */
    public void unhideIfHidden(long userId, long questionId) throws IOException {
        boolean dbOk = true;
        try {
            com.hourai.prts.service.WrongQuestionVisibilityDbService dbService = new com.hourai.prts.service.WrongQuestionVisibilityDbService();
            com.hourai.prts.entity.WrongQuestionVisibility wqv = new com.hourai.prts.entity.WrongQuestionVisibility();
            wqv.setUserId(userId);
            wqv.setQuestionId(questionId);
            wqv.setHidden(false);
            wqv.setUpdatedAt(java.time.LocalDateTime.now().toString());
            dbService.upsert(wqv);
        } catch (Exception e) {
            dbOk = false;
        }
        if (!dbOk) {
            visibilityDao.upsert(userId, questionId, false);
        }
    }

    private static Set<Long> computeWrongQuestionIds(long userId) throws IOException {
        // 仅CSV回退时用
        List<UserAnswer> uas = DataStore.loadUserAnswers();
        Set<Long> qids = uas.stream()
                .filter(a -> a.getUserId() == userId && !a.isCorrect())
                .map(UserAnswer::getQuestionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return qids;
    }
}
