package com.hourai.prts.service;

import com.hourai.prts.dao.UserAnswerDao;
import com.hourai.prts.entity.UserAnswer;
import java.sql.SQLException;
import java.util.List;

public class UserAnswerService {
    private final UserAnswerDao userAnswerDao = new UserAnswerDao();

    public int addUserAnswer(UserAnswer ua) throws SQLException {
        return userAnswerDao.insert(ua);
    }

    public UserAnswer getUserAnswerById(Long id) throws SQLException {
        return userAnswerDao.selectById(id);
    }

    public List<UserAnswer> getAllUserAnswers() throws SQLException {
        return userAnswerDao.selectAll();
    }

    public int updateUserAnswer(UserAnswer ua) throws SQLException {
        return userAnswerDao.update(ua);
    }

    public int deleteUserAnswer(Long id) throws SQLException {
        return userAnswerDao.delete(id);
    }
}
