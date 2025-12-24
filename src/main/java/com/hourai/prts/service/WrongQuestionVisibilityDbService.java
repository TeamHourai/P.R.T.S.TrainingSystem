package com.hourai.prts.service;

import com.hourai.prts.dao.WrongQuestionVisibilityDbDao;
import com.hourai.prts.entity.WrongQuestionVisibility;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WrongQuestionVisibilityDbService {
    private final WrongQuestionVisibilityDbDao dbDao = new WrongQuestionVisibilityDbDao();

    public int upsert(WrongQuestionVisibility wqv) throws SQLException {
        return dbDao.upsert(wqv);
    }

    public Set<Long> getHiddenQuestionIdsForUser(long userId) throws SQLException {
        Set<Long> ids = new HashSet<>();
        List<WrongQuestionVisibility> all = dbDao.selectAll();
        for (WrongQuestionVisibility w : all) {
            if (w.getUserId() == userId && w.isHidden()) ids.add(w.getQuestionId());
        }
        return ids;
    }

    public List<WrongQuestionVisibility> selectAll() throws SQLException {
        return dbDao.selectAll();
    }

    public WrongQuestionVisibility findByUserAndQuestion(long userId, long questionId) throws SQLException {
        return dbDao.findByUserAndQuestion(userId, questionId);
    }

    public long nextId() throws SQLException {
        return dbDao.nextId();
    }
}
