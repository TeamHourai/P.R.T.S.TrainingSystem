package com.hourai.prts.service;

import com.hourai.prts.dao.NotificationStateDao;
import com.hourai.prts.entity.NotificationState;
import java.sql.SQLException;
import java.util.List;

public class NotificationStateService {
    private final NotificationStateDao dao = new NotificationStateDao();

    public List<NotificationState> getStatesForUser(long userId) throws SQLException {
        return dao.selectByUserId(userId);
    }

    public int upsert(NotificationState ns) throws SQLException {
        return dao.upsert(ns);
    }
}
