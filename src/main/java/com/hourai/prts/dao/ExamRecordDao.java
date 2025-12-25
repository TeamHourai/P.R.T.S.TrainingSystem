
/**
 * 考试记录数据访问对象（DAO），负责对 exam_record 表进行增删改查操作。
 */
package com.hourai.prts.dao;

import com.hourai.prts.entity.ExamRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * ExamRecordDao 提供对考试记录表的数据库操作方法。
 */
public class ExamRecordDao {
    // ...existing code...
    /**
     * 新增考试记录
     * @param examRecord 实体
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
    // public int insert(ExamRecord examRecord) throws SQLException { ... }
    /**
     * 根据主键查询考试记录
     * @param id 考试记录ID
     * @return 实体或 null
     * @throws SQLException 数据库异常
     */
    // public ExamRecord selectById(Long id) throws SQLException { ... }
    /**
     * 查询所有考试记录
     * @return 实体列表
     * @throws SQLException 数据库异常
     */
    // public List<ExamRecord> selectAll() throws SQLException { ... }
    /**
     * 更新考试记录信息
     * @param examRecord 实体
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
    // public int update(ExamRecord examRecord) throws SQLException { ... }
    /**
     * 删除考试记录
     * @param id 考试记录ID
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
    // public int delete(Long id) throws SQLException { ... }
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public int insert(ExamRecord er) throws SQLException {
        // Build insert dynamically to tolerate schema differences (exam_name vs exam_id etc.)
        java.util.List<String> cols = new java.util.ArrayList<>();
        java.util.List<Object> vals = new java.util.ArrayList<>();
        // Handle id column: if id exists but is not auto-increment and not nullable, generate id when missing
        boolean idExists = DbCompat.columnExists("exam_records", "id");
        boolean includeId = er.getId() != null;
        if (!includeId && idExists && !DbCompat.isAutoIncrement("exam_records", "id") && !DbCompat.isNullable("exam_records", "id")) {
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id),0)+1 AS nid FROM exam_records")) {
                if (rs.next()) {
                    long nid = rs.getLong("nid");
                    er.setId(nid);
                    includeId = true;
                }
            }
        }
        if (includeId && idExists) { cols.add("id"); vals.add(er.getId()); }
        if (DbCompat.columnExists("exam_records", "user_id")) { cols.add("user_id"); vals.add(er.getUserId()); }
        if (DbCompat.columnExists("exam_records", "exam_name")) { cols.add("exam_name"); vals.add(er.getExamName()); }
        else if (DbCompat.columnExists("exam_records", "exam_id")) {
            Long examIdVal = null;
            if (er.getExamName() != null) {
                try {
                    examIdVal = Long.parseLong(er.getExamName());
                } catch (Exception ignored) {
                    examIdVal = null; // tolerate non-numeric examName
                }
            }
            cols.add("exam_id"); vals.add(examIdVal);
        }
        if (DbCompat.columnExists("exam_records", "total_questions")) { cols.add("total_questions"); vals.add(er.getTotalQuestions()); }
        if (DbCompat.columnExists("exam_records", "correct_count")) { cols.add("correct_count"); vals.add(er.getCorrectCount()); }
        if (DbCompat.columnExists("exam_records", "score")) { cols.add("score"); vals.add(er.getScore()); }
        if (DbCompat.columnExists("exam_records", "duration")) { cols.add("duration"); vals.add(er.getDuration()); }

        if (cols.isEmpty()) {
            throw new SQLException("No writable columns detected in exam_records");
        }

        StringBuilder colSb = new StringBuilder();
        StringBuilder phSb = new StringBuilder();
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) { colSb.append(", "); phSb.append(", "); }
            colSb.append(cols.get(i)); phSb.append("?");
        }
        String sql = "INSERT INTO exam_records (" + colSb.toString() + ") VALUES (" + phSb.toString() + ")";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < vals.size(); i++) {
                Object v = vals.get(i);
                int idx = i + 1;
                if (v == null) { ps.setNull(idx, Types.NULL); continue; }
                if (v instanceof Long) ps.setLong(idx, (Long) v);
                else if (v instanceof Integer) ps.setInt(idx, (Integer) v);
                else if (v instanceof java.math.BigDecimal) ps.setBigDecimal(idx, (java.math.BigDecimal) v);
                else ps.setString(idx, String.valueOf(v));
            }
            int rows = ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk != null && gk.next()) {
                    er.setId(gk.getLong(1));
                }
            }
            return rows;
        }
    }

    public ExamRecord selectById(Long id) throws SQLException {
        String sql = "SELECT * FROM exam_records WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ExamRecord er = new ExamRecord();
                ResultSetMetaData md = rs.getMetaData();
                boolean hasExamName = hasColumn(md, "exam_name");
                boolean hasExamId = hasColumn(md, "exam_id");
                boolean hasTotalQuestions = hasColumn(md, "total_questions");
                boolean hasCorrectCount = hasColumn(md, "correct_count");
                boolean hasScore = hasColumn(md, "score");
                boolean hasDuration = hasColumn(md, "duration");
                boolean hasCreatedAt = hasColumn(md, "created_at");
                boolean hasSubmitTime = hasColumn(md, "submit_time");

                er.setId(rs.getLong("id"));
                if (hasColumn(md, "user_id")) er.setUserId(rs.getLong("user_id"));
                if (hasExamName) er.setExamName(rs.getString("exam_name"));
                else if (hasExamId) er.setExamName(String.valueOf(rs.getLong("exam_id")));
                if (hasTotalQuestions) er.setTotalQuestions(rs.getInt("total_questions"));
                if (hasCorrectCount) er.setCorrectCount(rs.getInt("correct_count"));
                if (hasScore) er.setScore(rs.getBigDecimal("score"));
                if (hasDuration) er.setDuration(rs.getInt("duration"));
                if (hasCreatedAt) er.setCreatedAt(rs.getTimestamp("created_at"));
                else if (hasSubmitTime) er.setCreatedAt(rs.getTimestamp("submit_time"));
                return er;
            }
        }
        return null;
    }

    public List<ExamRecord> selectAll() throws SQLException {
        String sql = "SELECT * FROM exam_records";
        List<ExamRecord> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            boolean hasExamName = hasColumn(md, "exam_name");
            boolean hasExamId = hasColumn(md, "exam_id");
            boolean hasTotalQuestions = hasColumn(md, "total_questions");
            boolean hasCorrectCount = hasColumn(md, "correct_count");
            boolean hasScore = hasColumn(md, "score");
            boolean hasDuration = hasColumn(md, "duration");
            boolean hasCreatedAt = hasColumn(md, "created_at");
            boolean hasSubmitTime = hasColumn(md, "submit_time");

            while (rs.next()) {
                ExamRecord er = new ExamRecord();
                er.setId(rs.getLong("id"));
                if (hasColumn(md, "user_id")) er.setUserId(rs.getLong("user_id"));
                if (hasExamName) er.setExamName(rs.getString("exam_name"));
                else if (hasExamId) er.setExamName(String.valueOf(rs.getLong("exam_id")));
                if (hasTotalQuestions) er.setTotalQuestions(rs.getInt("total_questions"));
                if (hasCorrectCount) er.setCorrectCount(rs.getInt("correct_count"));
                if (hasScore) er.setScore(rs.getBigDecimal("score"));
                if (hasDuration) er.setDuration(rs.getInt("duration"));
                if (hasCreatedAt) er.setCreatedAt(rs.getTimestamp("created_at"));
                else if (hasSubmitTime) er.setCreatedAt(rs.getTimestamp("submit_time"));
                list.add(er);
            }
        }
        return list;
    }

    private boolean hasColumn(ResultSetMetaData md, String col) throws SQLException {
        int cnt = md.getColumnCount();
        for (int i = 1; i <= cnt; i++) {
            if (md.getColumnLabel(i).equalsIgnoreCase(col)) return true;
        }
        return false;
    }

    public int update(ExamRecord er) throws SQLException {
        String sql = "UPDATE exam_records SET user_id=?, exam_name=?, total_questions=?, correct_count=?, score=?, duration=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, er.getUserId());
            ps.setString(2, er.getExamName());
            ps.setInt(3, er.getTotalQuestions());
            ps.setInt(4, er.getCorrectCount());
            ps.setBigDecimal(5, er.getScore());
            if (er.getDuration() != null) {
                ps.setInt(6, er.getDuration());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setLong(7, er.getId());
            return ps.executeUpdate();
        }
    }

    public int delete(Long id) throws SQLException {
        String sql = "DELETE FROM exam_records WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }
}
