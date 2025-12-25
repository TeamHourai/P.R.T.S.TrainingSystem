package com.hourai.prts.dao;

import com.hourai.prts.entity.WrongQuestionVisibility;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WrongQuestionVisibilityDbDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public int upsert(WrongQuestionVisibility wqv) throws SQLException {
        String table = DbCompat.tableExists("wrong_visibility") ? "wrong_visibility"
            : (DbCompat.tableExists("wrong_question_visibility") ? "wrong_question_visibility" : "wrong_visibility");
        String hiddenCol = DbCompat.columnExists(table, "hidden") ? "hidden"
            : (DbCompat.columnExists(table, "visible") ? "visible" : "hidden");
        String updatedAtCol = DbCompat.columnExists(table, "updated_at") ? "updated_at"
            : (DbCompat.columnExists(table, "update_time") ? "update_time" : "updated_at");
        String sql = "REPLACE INTO " + table + " (id, user_id, question_id, " + hiddenCol + ", " + updatedAtCol + ") VALUES (?, ?, ?, ?, ?)";
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

    public List<WrongQuestionVisibility> selectAll() throws SQLException {
        List<WrongQuestionVisibility> out = new ArrayList<>();
        String table = DbCompat.tableExists("wrong_visibility") ? "wrong_visibility"
            : (DbCompat.tableExists("wrong_question_visibility") ? "wrong_question_visibility" : "wrong_visibility");
        String hiddenCol = DbCompat.columnExists(table, "hidden") ? "hidden"
                : (DbCompat.columnExists(table, "visible") ? "visible" : "hidden");
        String updatedAtCol = DbCompat.columnExists(table, "updated_at") ? "updated_at"
                : (DbCompat.columnExists(table, "update_time") ? "update_time" : "updated_at");
        String sql = "SELECT id, user_id, question_id, " + hiddenCol + ", " + updatedAtCol + " FROM " + table + " ORDER BY id";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                long userId = rs.getLong("user_id");
                long questionId = rs.getLong("question_id");
                boolean hidden = rs.getBoolean(hiddenCol);
                String updatedAt = rs.getString(updatedAtCol);
                out.add(new WrongQuestionVisibility(id, userId, questionId, hidden, updatedAt));
            }
        }
        return out;
    }

    public WrongQuestionVisibility findByUserAndQuestion(long userId, long questionId) throws SQLException {
        String table = DbCompat.tableExists("wrong_visibility") ? "wrong_visibility"
            : (DbCompat.tableExists("wrong_question_visibility") ? "wrong_question_visibility" : "wrong_visibility");
        String hiddenCol = DbCompat.columnExists(table, "hidden") ? "hidden"
                : (DbCompat.columnExists(table, "visible") ? "visible" : "hidden");
        String updatedAtCol = DbCompat.columnExists(table, "updated_at") ? "updated_at"
                : (DbCompat.columnExists(table, "update_time") ? "update_time" : "updated_at");
        String sql = "SELECT id, user_id, question_id, " + hiddenCol + ", " + updatedAtCol + " FROM " + table + " WHERE user_id = ? AND question_id = ? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    boolean hidden = rs.getBoolean(hiddenCol);
                    String updatedAt = rs.getString(updatedAtCol);
                    return new WrongQuestionVisibility(id, userId, questionId, hidden, updatedAt);
                }
            }
        }
        return null;
    }

    public long nextId() throws SQLException {
        String table = DbCompat.tableExists("wrong_visibility") ? "wrong_visibility"
            : (DbCompat.tableExists("wrong_question_visibility") ? "wrong_question_visibility" : "wrong_visibility");
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM " + table;
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong("next_id");
        }
        return 1L;
    }
}
