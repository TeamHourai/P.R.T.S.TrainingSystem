package com.hourai.prts.dao;

import com.hourai.prts.data.DataStore;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class TrainingRecordDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public Map<Long, DataStore.TrainingRecord> selectByUserId(long userId) throws SQLException {
        String sql = "SELECT question_id, attempts, correct, last_at FROM training_records WHERE user_id = ?";
        Map<Long, DataStore.TrainingRecord> out = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long qid = rs.getLong("question_id");
                    int attempts = rs.getInt("attempts");
                    boolean correct = rs.getBoolean("correct");
                    long lastAt = rs.getLong("last_at");
                    out.put(qid, new DataStore.TrainingRecord(qid, attempts, correct, lastAt));
                }
            }
        }
        return out;
    }

    public int upsert(long userId, long questionId, int attempts, boolean correct, long lastAt) throws SQLException {
        // Ensure table has unique key on (user_id, question_id)
        String sql = "INSERT INTO training_records (user_id, question_id, attempts, correct, last_at) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE attempts = VALUES(attempts), correct = VALUES(correct), last_at = VALUES(last_at)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            ps.setInt(3, attempts);
            ps.setBoolean(4, correct);
            ps.setLong(5, lastAt);
            return ps.executeUpdate();
        }
    }

    public int clearByUserId(long userId) throws SQLException {
        String sql = "DELETE FROM training_records WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate();
        }
    }
}
