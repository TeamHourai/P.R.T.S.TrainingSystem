package com.hourai.prts.service;

import com.hourai.prts.dao.OnboardingQuestionDao;
import com.hourai.prts.dao.QuestionDao;
import com.hourai.prts.entity.OnboardingQuestion;
import com.hourai.prts.entity.Question;
import java.sql.SQLException;
import java.util.List;

public class QuestionService {
    private final QuestionDao questionDao = new QuestionDao();
    private final OnboardingQuestionDao onboardingDao = new OnboardingQuestionDao();

    public List<Question> getAllQuestionsByType(int type) throws SQLException {
        return questionDao.selectAllByType(type);
    }

    public int addQuestion(Question q) throws SQLException {
        return questionDao.insert(q);
    }

    public Question getQuestionById(Long id) throws SQLException {
        return questionDao.selectById(id);
    }

    public List<Question> getAllQuestions() throws SQLException {
        return questionDao.selectAll();
    }

    public int updateQuestion(Question q) throws SQLException {
        return questionDao.update(q);
    }

    public int deleteQuestion(Long id) throws SQLException {
        return questionDao.delete(id);
    }

    // --- Onboarding-specific APIs (separate handling) ---
    public List<OnboardingQuestion> getAllOnboardingQuestions() throws SQLException {
        return onboardingDao.selectAll();
    }

    public List<OnboardingQuestion> getOnboardingByGroup(int groupId) throws SQLException {
        return onboardingDao.selectByGroupId(groupId);
    }

    public OnboardingQuestion getOnboardingById(int id) throws SQLException {
        return onboardingDao.selectById(id);
    }

    public int addOnboardingQuestion(OnboardingQuestion q) throws SQLException {
        return onboardingDao.insert(q);
    }

    public int updateOnboardingQuestion(OnboardingQuestion q) throws SQLException {
        return onboardingDao.update(q);
    }

    public int deleteOnboardingQuestion(int id) throws SQLException {
        return onboardingDao.delete(id);
    }
}
