package com.hourai.prts.service;

import com.hourai.prts.dao.AnswerSettingsDao;
import com.hourai.prts.data.DataStore;
import java.sql.SQLException;

public class AnswerSettingsService {
    private final AnswerSettingsDao dao = new AnswerSettingsDao();

    public DataStore.AnswerSettings getForUser(long userId) throws SQLException {
        return dao.selectByUserId(userId);
    }

    public DataStore.AnswerSettings upsert(long userId, boolean autoSubmit, boolean autoNextCorrect) throws SQLException {
        dao.upsert(userId, autoSubmit, autoNextCorrect);
        return new DataStore.AnswerSettings(autoSubmit, autoNextCorrect);
    }
}
