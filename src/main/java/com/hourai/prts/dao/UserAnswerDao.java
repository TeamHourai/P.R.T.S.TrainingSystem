
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
        // Build insert dynamically based on which columns actually exist to tolerate schema differences
        java.util.List<String> cols = new java.util.ArrayList<>();
        java.util.List<Object> vals = new java.util.ArrayList<>();
        boolean includeId = ua.getId() != null;

        // If id column exists but is not auto-increment and not nullable, we must generate an id when not provided.
        boolean idExists = DbCompat.columnExists("user_answers", "id");
        if (!includeId && idExists && !DbCompat.isAutoIncrement("user_answers", "id") && !DbCompat.isNullable("user_answers", "id")) {
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id),0)+1 AS nid FROM user_answers")) {
                if (rs.next()) {
                    long nid = rs.getLong("nid");
                    ua.setId(nid);
                    includeId = true;
                }
            }
        }

        if (includeId) { cols.add("id"); vals.add(ua.getId()); }
        cols.add("user_id"); vals.add(ua.getUserId());
        cols.add("question_id"); vals.add(ua.getQuestionId());

        // selected answer
        String selCol = DbCompat.columnExists("user_answers", "selected_answer") ? "selected_answer"
                : (DbCompat.columnExists("user_answers", "selected_option") ? "selected_option"
                : (DbCompat.columnExists("user_answers", "selected") ? "selected" : null));
        if (selCol != null) { cols.add(selCol); vals.add(ua.getSelectedAnswer()); }

        // correctness
        String correctCol = DbCompat.columnExists("user_answers", "is_correct") ? "is_correct"
                : (DbCompat.columnExists("user_answers", "correct") ? "correct" : null);
        if (correctCol != null) { cols.add(correctCol); vals.add(ua.isCorrect()); }

        // answer time
        String answerTimeCol = DbCompat.columnExists("user_answers", "answer_time") ? "answer_time"
                : (DbCompat.columnExists("user_answers", "submit_time") ? "submit_time" : null);
        if (answerTimeCol != null) { cols.add(answerTimeCol); vals.add(ua.getAnswerTime()); }

        // created / submit timestamp
        String createdAtCol = DbCompat.columnExists("user_answers", "created_at") ? "created_at"
                : (DbCompat.columnExists("user_answers", "submit_time") ? "submit_time" : null);
        if (createdAtCol != null) { cols.add(createdAtCol); vals.add(ua.getCreatedAt()); }

        if (cols.size() < 3) {
            // unexpected schema: fallback to simple insert with minimal columns
            String sql = "INSERT INTO user_answers (user_id, question_id) VALUES (?, ?)";
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, ua.getUserId());
                ps.setLong(2, ua.getQuestionId());
                int rows = ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) { if (gk != null && gk.next()) ua.setId(gk.getLong(1)); }
                return rows;
            }
        }

        StringBuilder colSb = new StringBuilder();
        StringBuilder phSb = new StringBuilder();
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) { colSb.append(", "); phSb.append(", "); }
            colSb.append(cols.get(i)); phSb.append("?");
        }
        String sql = "INSERT INTO user_answers (" + colSb.toString() + ") VALUES (" + phSb.toString() + ")";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < vals.size(); i++) {
                Object v = vals.get(i);
                int idx = i + 1;
                if (v == null) { ps.setNull(idx, Types.NULL); continue; }
                if (v instanceof Long) ps.setLong(idx, (Long) v);
                else if (v instanceof Integer) ps.setInt(idx, (Integer) v);
                else if (v instanceof Boolean) ps.setBoolean(idx, (Boolean) v);
                else if (v instanceof Timestamp) ps.setTimestamp(idx, (Timestamp) v);
                else ps.setString(idx, String.valueOf(v));
            }
            int rows = ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk != null && gk.next()) ua.setId(gk.getLong(1));
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
