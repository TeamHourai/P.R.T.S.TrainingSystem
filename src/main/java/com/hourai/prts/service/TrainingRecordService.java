package com.hourai.prts.service;

import com.hourai.prts.dao.TrainingRecordDao;
import com.hourai.prts.entity.TrainingRecord;
import java.sql.SQLException;
import java.util.Map;

public class TrainingRecordService {
    private final TrainingRecordDao dao = new TrainingRecordDao();

    public Map<Long, TrainingRecord> getRecordsForUser(long userId) throws SQLException {
        return dao.selectByUserId(userId);
    }

    public TrainingRecord upsert(long userId, long questionId, int attempts, boolean correct, long lastAt) throws SQLException {
        dao.upsert(userId, questionId, attempts, correct, lastAt);
        return new TrainingRecord(questionId, attempts, correct, lastAt);
    }

    public void clearForUser(long userId) throws SQLException {
        dao.clearByUserId(userId);
    }
}
