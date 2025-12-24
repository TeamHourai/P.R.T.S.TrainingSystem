package com.hourai.prts.dao;

import com.hourai.prts.data.DataStore;
import java.sql.*;

public class AnswerSettingsDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public DataStore.AnswerSettings selectByUserId(long userId) throws SQLException {
        String sql = "SELECT auto_submit, auto_next_correct FROM answer_settings WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean autoSubmit = rs.getBoolean("auto_submit");
                    boolean autoNext = rs.getBoolean("auto_next_correct");
                    return new DataStore.AnswerSettings(autoSubmit, autoNext);
                }
            }
        }
        return new DataStore.AnswerSettings(false, false);
    }

    public int upsert(long userId, boolean autoSubmit, boolean autoNextCorrect) throws SQLException {
        String sql = "INSERT INTO answer_settings (user_id, auto_submit, auto_next_correct, updated_at) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE auto_submit = VALUES(auto_submit), auto_next_correct = VALUES(auto_next_correct), updated_at = VALUES(updated_at)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setBoolean(2, autoSubmit);
            ps.setBoolean(3, autoNextCorrect);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate();
        }
    }
}
