
/**
 * 考试记录数据访问对象（DAO），负责对 exam_record 表进行增删改查操作。
 */
package com.hourai.prts.dao;

import com.hourai.prts.entity.ExamRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "INSERT INTO exam_record (user_id, exam_name, total_questions, correct_count, score, duration) VALUES (?, ?, ?, ?, ?, ?)";
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
            return ps.executeUpdate();
        }
    }

    public ExamRecord selectById(Long id) throws SQLException {
        String sql = "SELECT * FROM exam_record WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ExamRecord er = new ExamRecord();
                er.setId(rs.getLong("id"));
                er.setUserId(rs.getLong("user_id"));
                er.setExamName(rs.getString("exam_name"));
                er.setTotalQuestions(rs.getInt("total_questions"));
                er.setCorrectCount(rs.getInt("correct_count"));
                er.setScore(rs.getBigDecimal("score"));
                er.setDuration(rs.getInt("duration"));
                er.setCreatedAt(rs.getTimestamp("created_at"));
                return er;
            }
        }
        return null;
    }

    public List<ExamRecord> selectAll() throws SQLException {
        String sql = "SELECT * FROM exam_record";
        List<ExamRecord> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ExamRecord er = new ExamRecord();
                er.setId(rs.getLong("id"));
                er.setUserId(rs.getLong("user_id"));
                er.setExamName(rs.getString("exam_name"));
                er.setTotalQuestions(rs.getInt("total_questions"));
                er.setCorrectCount(rs.getInt("correct_count"));
                er.setScore(rs.getBigDecimal("score"));
                er.setDuration(rs.getInt("duration"));
                er.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(er);
            }
        }
        return list;
    }

    public int update(ExamRecord er) throws SQLException {
        String sql = "UPDATE exam_record SET user_id=?, exam_name=?, total_questions=?, correct_count=?, score=?, duration=? WHERE id=?";
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
        String sql = "DELETE FROM exam_record WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }
}
