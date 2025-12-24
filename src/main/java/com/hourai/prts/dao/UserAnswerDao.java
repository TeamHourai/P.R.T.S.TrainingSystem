
/**
 * 用户答题数据访问对象（DAO），负责对 user_answer 表进行增删改查操作。
 */
package com.hourai.prts.dao;

import com.hourai.prts.entity.UserAnswer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserAnswerDao 提供对用户答题表的数据库操作方法。
 */
public class UserAnswerDao {
    // ...existing code...
    /**
     * 新增用户答题记录
     * @param userAnswer 实体
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
    // public int insert(UserAnswer userAnswer) throws SQLException { ... }
    /**
     * 根据主键查询用户答题
     * @param id 用户答题ID
     * @return 实体或 null
     * @throws SQLException 数据库异常
     */
    // public UserAnswer selectById(Long id) throws SQLException { ... }
    /**
     * 查询所有用户答题
     * @return 实体列表
     * @throws SQLException 数据库异常
     */
    // public List<UserAnswer> selectAll() throws SQLException { ... }
    /**
     * 更新用户答题信息
     * @param userAnswer 实体
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
    // public int update(UserAnswer userAnswer) throws SQLException { ... }
    /**
     * 删除用户答题
     * @param id 用户答题ID
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
    // public int delete(Long id) throws SQLException { ... }
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public int insert(UserAnswer ua) throws SQLException {
        // If an explicit id is provided (e.g. during CSV import), insert with id.
        if (ua.getId() != null) {
            String sqlWithId = "INSERT INTO user_answers (id, user_id, question_id, selected_answer, is_correct, answer_time, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement ps = conn.prepareStatement(sqlWithId)) {
                ps.setLong(1, ua.getId());
                ps.setLong(2, ua.getUserId());
                ps.setLong(3, ua.getQuestionId());
                ps.setString(4, ua.getSelectedAnswer());
                ps.setBoolean(5, ua.isCorrect());
                if (ua.getAnswerTime() != null) ps.setInt(6, ua.getAnswerTime());
                else ps.setNull(6, Types.INTEGER);
                if (ua.getCreatedAt() != null) ps.setTimestamp(7, ua.getCreatedAt());
                else ps.setNull(7, Types.TIMESTAMP);
                return ps.executeUpdate();
            }
        }

        String sql = "INSERT INTO user_answers (user_id, question_id, selected_answer, is_correct, answer_time, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, ua.getUserId());
            ps.setLong(2, ua.getQuestionId());
            ps.setString(3, ua.getSelectedAnswer());
            ps.setBoolean(4, ua.isCorrect());
            if (ua.getAnswerTime() != null) ps.setInt(5, ua.getAnswerTime());
            else ps.setNull(5, Types.INTEGER);
            if (ua.getCreatedAt() != null) ps.setTimestamp(6, ua.getCreatedAt());
            else ps.setNull(6, Types.TIMESTAMP);
            int rows = ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk != null && gk.next()) {
                    ua.setId(gk.getLong(1));
                }
            }
            return rows;
        }
    }

    public UserAnswer selectById(Long id) throws SQLException {
        String sql = "SELECT * FROM user_answers WHERE id = ?";
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
        String sql = "SELECT * FROM user_answers";
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
        String sql = "UPDATE user_answers SET user_id=?, question_id=?, selected_answer=?, is_correct=?, answer_time=? WHERE id=?";
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
        String sql = "DELETE FROM user_answers WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }
}
