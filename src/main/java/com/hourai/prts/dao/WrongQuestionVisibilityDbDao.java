package com.hourai.prts.dao;

import com.hourai.prts.entity.WrongQuestionVisibility;
import java.sql.*;

public class WrongQuestionVisibilityDbDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public int upsert(WrongQuestionVisibility wqv) throws SQLException {
        String sql = "REPLACE INTO wrong_question_visibility (id, user_id, question_id, hidden, updated_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, wqv.getId());
            ps.setLong(2, wqv.getUserId());
            ps.setLong(3, wqv.getQuestionId());
            ps.setBoolean(4, wqv.isHidden());
            ps.setString(5, wqv.getUpdatedAt());
            return ps.executeUpdate();
        }
    }
}
