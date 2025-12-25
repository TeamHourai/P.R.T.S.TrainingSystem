
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
        // Determine column names to use based on DB schema
        String selCol = DbCompat.columnExists("user_answers", "selected_answer") ? "selected_answer"
                : (DbCompat.columnExists("user_answers", "answer") ? "answer" : "selected_answer");
        String correctCol = DbCompat.columnExists("user_answers", "is_correct") ? "is_correct"
                : (DbCompat.columnExists("user_answers", "correct") ? "correct" : "is_correct");
        String answerTimeCol = DbCompat.columnExists("user_answers", "answer_time") ? "answer_time"
                : (DbCompat.columnExists("user_answers", "submit_time") ? "submit_time" : "answer_time");
        String createdAtCol = DbCompat.columnExists("user_answers", "created_at") ? "created_at"
                : (DbCompat.columnExists("user_answers", "submit_time") ? "submit_time" : "created_at");

        // If explicit id provided
        if (ua.getId() != null) {
            String sqlWithId = String.format("INSERT INTO user_answers (id, user_id, question_id, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    selCol, correctCol, answerTimeCol, createdAtCol);
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

        String sql = String.format("INSERT INTO user_answers (user_id, question_id, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)",
                selCol, correctCol, answerTimeCol, createdAtCol);
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
                    ua.setSelectedAnswer(readSelectedAnswer(rs));
                    ua.setCorrect(readCorrect(rs));
                ua.setAnswerTime(readAnswerTime(rs));
                ua.setCreatedAt(readCreatedAt(rs));
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
                    ua.setSelectedAnswer(readSelectedAnswer(rs));
                    ua.setCorrect(readCorrect(rs));
                ua.setAnswerTime(readAnswerTime(rs));
                ua.setCreatedAt(readCreatedAt(rs));
                list.add(ua);
            }
        }
        return list;
    }
        // Helper: read correctness flag from ResultSet trying multiple candidate column names.
        private boolean readCorrect(ResultSet rs) {
            try {
                if (hasColumn(rs, "is_correct")) return rs.getBoolean("is_correct");
                if (hasColumn(rs, "correct")) return rs.getBoolean("correct");
                if (hasColumn(rs, "isCorrect")) return rs.getBoolean("isCorrect");
            } catch (SQLException ignored) { }
            return false;
        }

        // Helper: read answer time (may be stored as INT or as DATETIME/submit_time)
        private Integer readAnswerTime(ResultSet rs) {
            try {
                if (hasColumn(rs, "answer_time")) {
                    try {
                        int v = rs.getInt("answer_time");
                        if (rs.wasNull()) return null;
                        return v;
                    } catch (SQLException ex) {
                        Object o = rs.getObject("answer_time");
                        if (o instanceof Number) return ((Number) o).intValue();
                        if (o instanceof String) {
                            try { return Integer.parseInt((String)o); } catch (Exception ignored) { }
                        }
                        return null;
                    }
                }
                if (hasColumn(rs, "submit_time")) {
                    try {
                        int v = rs.getInt("submit_time");
                        if (!rs.wasNull()) return v;
                    } catch (SQLException ex) {
                        // submit_time may be DATETIME - not convertible to int
                    }
                }
            } catch (SQLException ignored) { }
            return null;
        }

        // Helper: read created timestamp from ResultSet using possible column names
        private Timestamp readCreatedAt(ResultSet rs) {
            try {
                if (hasColumn(rs, "created_at")) return rs.getTimestamp("created_at");
                if (hasColumn(rs, "submit_time")) return rs.getTimestamp("submit_time");
            } catch (SQLException ignored) { }
            return null;
        }

    // Helper: try multiple candidate column names for selected answer to tolerate schema variations.
    private String readSelectedAnswer(ResultSet rs) {
        try {
            if (hasColumn(rs, "selected_answer")) return rs.getString("selected_answer");
            if (hasColumn(rs, "selected_option")) return rs.getString("selected_option");
            if (hasColumn(rs, "selectedOption")) return rs.getString("selectedOption");
            if (hasColumn(rs, "selected")) return rs.getString("selected");
        } catch (SQLException ignored) { }
        return null;
    }

    private boolean hasColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public int update(UserAnswer ua) throws SQLException {
        String selCol = DbCompat.columnExists("user_answers", "selected_answer") ? "selected_answer"
                : (DbCompat.columnExists("user_answers", "answer") ? "answer" : "selected_answer");
        String correctCol = DbCompat.columnExists("user_answers", "is_correct") ? "is_correct"
                : (DbCompat.columnExists("user_answers", "correct") ? "correct" : "is_correct");
        String answerTimeCol = DbCompat.columnExists("user_answers", "answer_time") ? "answer_time"
                : (DbCompat.columnExists("user_answers", "submit_time") ? "submit_time" : "answer_time");

        String sql = String.format("UPDATE user_answers SET user_id=?, question_id=?, %s=?, %s=?, %s=? WHERE id=?", selCol, correctCol, answerTimeCol);
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
