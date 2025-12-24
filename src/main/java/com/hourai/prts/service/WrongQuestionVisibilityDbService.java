package com.hourai.prts.service;

import com.hourai.prts.dao.WrongQuestionVisibilityDbDao;
import com.hourai.prts.entity.WrongQuestionVisibility;
import java.sql.SQLException;

public class WrongQuestionVisibilityDbService {
    private final WrongQuestionVisibilityDbDao dbDao = new WrongQuestionVisibilityDbDao();

    public int upsert(WrongQuestionVisibility wqv) throws SQLException {
        return dbDao.upsert(wqv);
    }
}
