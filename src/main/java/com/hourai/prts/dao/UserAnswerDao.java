package com.hourai.prts.dao;

import com.hourai.prts.entity.UserAnswer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAnswerDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public int insert(UserAnswer ua) throws SQLException {
        String sql = "INSERT INTO user_answer (user_id, question_id, selected_answer, is_correct, answer_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ua.getUserId());
            ps.setLong(2, ua.getQuestionId());
            ps.setString(3, ua.getSelectedAnswer());
            ps.setBoolean(4, ua.isCorrect());
            if (ua.getAnswerTime() != null) {
                ps.setInt(5, ua.getAnswerTime());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps.executeUpdate();
        }
    }

    public UserAnswer selectById(Long id) throws SQLException {
        String sql = "SELECT * FROM user_answer WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UserAnswer ua = new UserAnswer();
                ua.setId(rs.getLong("id"));
                ua.setUserId(rs.getLong("user_id"));
                ua.setQuestionId(rs.getLong("question_id"));
                ua.setSelectedAnswer(rs.getString("selected_answer"));
                ua.setCorrect(rs.getBoolean("is_correct"));
                ua.setAnswerTime(rs.getInt("answer_time"));
                ua.setCreatedAt(rs.getTimestamp("created_at"));
                return ua;
            }
        }
        return null;
    }

    public List<UserAnswer> selectAll() throws SQLException {
        String sql = "SELECT * FROM user_answer";
        List<UserAnswer> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                UserAnswer ua = new UserAnswer();
                ua.setId(rs.getLong("id"));
                ua.setUserId(rs.getLong("user_id"));
                ua.setQuestionId(rs.getLong("question_id"));
                ua.setSelectedAnswer(rs.getString("selected_answer"));
                ua.setCorrect(rs.getBoolean("is_correct"));
                ua.setAnswerTime(rs.getInt("answer_time"));
                ua.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(ua);
            }
        }
        return list;
    }

    public int update(UserAnswer ua) throws SQLException {
        String sql = "UPDATE user_answer SET user_id=?, question_id=?, selected_answer=?, is_correct=?, answer_time=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ua.getUserId());
            ps.setLong(2, ua.getQuestionId());
            ps.setString(3, ua.getSelectedAnswer());
            ps.setBoolean(4, ua.isCorrect());
            if (ua.getAnswerTime() != null) {
                ps.setInt(5, ua.getAnswerTime());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setLong(6, ua.getId());
            return ps.executeUpdate();
        }
    }

    public int delete(Long id) throws SQLException {
        String sql = "DELETE FROM user_answer WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }
}
