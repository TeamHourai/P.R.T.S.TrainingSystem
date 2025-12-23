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
        Set<Long> wrongIds = computeWrongQuestionIds(userId);
        Set<Long> hiddenIds = visibilityDao.selectAll().stream()
                .filter(r -> r.getUserId() == userId && r.isHidden())
                .map(r -> r.getQuestionId())
                .collect(Collectors.toSet());
        wrongIds.removeAll(hiddenIds);
        return DataStore.loadQuestions().stream().filter(q -> wrongIds.contains(q.getId())).collect(Collectors.toList());
    }

    /**
     * Hides a question from the wrong list for the user.
     */
    public void hideWrongQuestion(long userId, long questionId) throws IOException {
        visibilityDao.upsert(userId, questionId, true);
    }

    /**
     * If the question was previously hidden from the wrong list, unhide it.
     *
     * Rule: if the user deletes a wrong question but later answers it wrong again,
     * the hide mark should be cleared.
     */
    public void unhideIfHidden(long userId, long questionId) throws IOException {
        visibilityDao.upsert(userId, questionId, false);
    }

    private static Set<Long> computeWrongQuestionIds(long userId) throws IOException {
        List<UserAnswer> uas = DataStore.loadUserAnswers();
        Set<Long> qids = uas.stream()
                .filter(a -> a.getUserId() == userId && !a.isCorrect())
                .map(UserAnswer::getQuestionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return qids;
    }
}
