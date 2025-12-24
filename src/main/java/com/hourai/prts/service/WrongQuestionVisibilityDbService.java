package com.hourai.prts.service;

import com.hourai.prts.dao.WrongQuestionVisibilityDbDao;
import com.hourai.prts.entity.WrongQuestionVisibility;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.sql.*;

public class WrongQuestionVisibilityDbService {
    private final WrongQuestionVisibilityDbDao dbDao = new WrongQuestionVisibilityDbDao();

    public int upsert(WrongQuestionVisibility wqv) throws SQLException {
        return dbDao.upsert(wqv);
    }

    public Set<Long> getHiddenQuestionIdsForUser(long userId) throws SQLException {
        Set<Long> ids = new HashSet<>();
        String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "p.r.t.s.data115";
        String sql = "SELECT question_id FROM wrong_question_visibility WHERE user_id = ? AND hidden = 1";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("question_id"));
                }
            }
        }
        return ids;
    }
}
