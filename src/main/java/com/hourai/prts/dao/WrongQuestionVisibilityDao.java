package com.hourai.prts.dao;

import com.hourai.prts.entity.WrongQuestionVisibility;
import com.hourai.prts.service.WrongQuestionVisibilityDbService;
import com.hourai.prts.utils.Utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * DAO adapter: previously CSV-backed, now delegates to DB service for runtime.
 * CSV import tools may still use DataStore; runtime paths should use DB.
 */
public class WrongQuestionVisibilityDao {

    private final WrongQuestionVisibilityDbService dbService = new WrongQuestionVisibilityDbService();

    public synchronized List<WrongQuestionVisibility> selectAll() throws IOException {
        try {
            return dbService.selectAll();
        } catch (SQLException e) {
            throw new IOException("Failed to load wrong question visibility from DB", e);
        }
    }

    public synchronized Optional<WrongQuestionVisibility> findByUserAndQuestion(long userId, long questionId) throws IOException {
        try {
            return Optional.ofNullable(dbService.findByUserAndQuestion(userId, questionId));
        } catch (SQLException e) {
            throw new IOException("Failed to query wrong question visibility", e);
        }
    }

    /**
     * Upsert a visibility record (DB-backed). Returns the saved entity.
     */
    public synchronized WrongQuestionVisibility upsert(long userId, long questionId, boolean hidden) throws IOException {
        try {
            Optional<WrongQuestionVisibility> existing = Optional.ofNullable(dbService.findByUserAndQuestion(userId, questionId));
            String now = Utils.now();
            WrongQuestionVisibility target;
            if (existing.isPresent()) {
                target = existing.get();
                target.setHidden(hidden);
                target.setUpdatedAt(now);
            } else {
                long id = dbService.nextId();
                target = new WrongQuestionVisibility(id, userId, questionId, hidden, now);
            }
            dbService.upsert(target);
            return Optional.ofNullable(dbService.findByUserAndQuestion(userId, questionId)).orElseThrow();
        } catch (SQLException e) {
            throw new IOException("Failed to upsert wrong question visibility", e);
        }
    }
}

