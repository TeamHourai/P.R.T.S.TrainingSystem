package com.hourai.prts.service;

import com.hourai.prts.entity.Question;
import com.hourai.prts.entity.UserAnswer;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for wrong-question list — DB-only.
 */
public class WrongQuestionService {
    /**
     * Returns wrong questions for a user, filtered by visibility (hidden=false).
     */
    public List<Question> getVisibleWrongQuestions(long userId) throws IOException {
        try {
            UserAnswerService userAnswerService = new UserAnswerService();
            WrongQuestionVisibilityDbService dbService = new WrongQuestionVisibilityDbService();
            // 查找所有答错的题目id
            List<UserAnswer> uas = userAnswerService.getAllUserAnswers();
            Set<Long> wrongIds = uas.stream()
                    .filter(a -> a.getUserId() != null && a.getUserId() == userId && !a.isCorrect())
                    .map(UserAnswer::getQuestionId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            // 查找所有隐藏的题目id
            Set<Long> hiddenIds = dbService.getHiddenQuestionIdsForUser(userId);
            // 查找所有题目
            QuestionService questionService = new QuestionService();
            List<Question> questions = questionService.getAllQuestions();

            wrongIds.removeAll(hiddenIds);
            Set<Long> finalWrongIds = wrongIds;
            return questions.stream().filter(q -> finalWrongIds.contains(q.getId())).collect(Collectors.toList());
        } catch (Exception e) {
            throw new IOException("Failed to load wrong questions from DB: " + e.getMessage(), e);
        }
    }

    public void hideWrongQuestion(long userId, long questionId) throws IOException {
        try {
            WrongQuestionVisibilityDbService dbService = new WrongQuestionVisibilityDbService();
            com.hourai.prts.entity.WrongQuestionVisibility wqv = new com.hourai.prts.entity.WrongQuestionVisibility();
            wqv.setUserId(userId);
            wqv.setQuestionId(questionId);
            wqv.setHidden(true);
            wqv.setUpdatedAt(java.time.LocalDateTime.now().toString());
            dbService.upsert(wqv);
        } catch (Exception e) {
            throw new IOException("Failed to hide wrong question: " + e.getMessage(), e);
        }
    }

    public void unhideIfHidden(long userId, long questionId) throws IOException {
        try {
            WrongQuestionVisibilityDbService dbService = new WrongQuestionVisibilityDbService();
            com.hourai.prts.entity.WrongQuestionVisibility wqv = new com.hourai.prts.entity.WrongQuestionVisibility();
            wqv.setUserId(userId);
            wqv.setQuestionId(questionId);
            wqv.setHidden(false);
            wqv.setUpdatedAt(java.time.LocalDateTime.now().toString());
            dbService.upsert(wqv);
        } catch (Exception e) {
            throw new IOException("Failed to unhide wrong question: " + e.getMessage(), e);
        }
    }
}
