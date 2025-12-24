package com.hourai.prts.service;

import com.hourai.prts.dao.QuestionDao;
import com.hourai.prts.entity.Question;
import java.sql.SQLException;
import java.util.List;

public class QuestionService {
        public List<Question> getAllQuestionsByType(int type) throws SQLException {
            return questionDao.selectAllByType(type);
        }
    private final QuestionDao questionDao = new QuestionDao();

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
}
