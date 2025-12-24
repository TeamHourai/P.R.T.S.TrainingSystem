
/**
 * 试卷详情数据访问对象（DAO），负责对 exam_detail 表进行增删改查操作。
 */
package com.hourai.prts.dao;

import com.hourai.prts.entity.ExamDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ExamDetailDao 提供对试卷详情表的数据库操作方法。
 */
public class ExamDetailDao {
    // ...existing code...
    // 建议为每个方法补充类似注释：
    /**
     * 新增试卷详情记录
     * @param examDetail 实体
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
    // public int insert(ExamDetail examDetail) throws SQLException { ... }
    /**
     * 根据主键查询试卷详情
     * @param id 试卷详情ID
     * @return 实体或 null
     * @throws SQLException 数据库异常
     */
    // public ExamDetail selectById(Long id) throws SQLException { ... }
    /**
     * 查询所有试卷详情
     * @return 实体列表
     * @throws SQLException 数据库异常
     */
    // public List<ExamDetail> selectAll() throws SQLException { ... }
    /**
     * 更新试卷详情信息
     * @param examDetail 实体
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
    // public int update(ExamDetail examDetail) throws SQLException { ... }
    /**
     * 删除试卷详情
     * @param id 试卷详情ID
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
    // public int delete(Long id) throws SQLException { ... }
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public int insert(ExamDetail ed) throws SQLException {
        String sql = "INSERT INTO exam_detail (exam_id, question_id, selected_answer, is_correct) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ed.getExamId());
            ps.setLong(2, ed.getQuestionId());
            ps.setString(3, ed.getSelectedAnswer());
            if (ed.getIsCorrect() != null) {
                ps.setBoolean(4, ed.getIsCorrect());
            } else {
                ps.setNull(4, Types.BOOLEAN);
            }
            return ps.executeUpdate();
        }
    }

    public ExamDetail selectById(Long id) throws SQLException {
        String sql = "SELECT * FROM exam_detail WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ExamDetail ed = new ExamDetail();
                ed.setId(rs.getLong("id"));
                ed.setExamId(rs.getLong("exam_id"));
                ed.setQuestionId(rs.getLong("question_id"));
                ed.setSelectedAnswer(rs.getString("selected_answer"));
                ed.setIsCorrect((Boolean)rs.getObject("is_correct"));
                return ed;
            }
        }
        return null;
    }

    public List<ExamDetail> selectAll() throws SQLException {
        String sql = "SELECT * FROM exam_detail";
        List<ExamDetail> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ExamDetail ed = new ExamDetail();
                ed.setId(rs.getLong("id"));
                ed.setExamId(rs.getLong("exam_id"));
                ed.setQuestionId(rs.getLong("question_id"));
                ed.setSelectedAnswer(rs.getString("selected_answer"));
                ed.setIsCorrect((Boolean)rs.getObject("is_correct"));
                list.add(ed);
            }
        }
        return list;
    }

    public int update(ExamDetail ed) throws SQLException {
        String sql = "UPDATE exam_detail SET exam_id=?, question_id=?, selected_answer=?, is_correct=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ed.getExamId());
            ps.setLong(2, ed.getQuestionId());
            ps.setString(3, ed.getSelectedAnswer());
            if (ed.getIsCorrect() != null) {
                ps.setBoolean(4, ed.getIsCorrect());
            } else {
                ps.setNull(4, Types.BOOLEAN);
            }
            ps.setLong(5, ed.getId());
            return ps.executeUpdate();
        }
    }

    public int delete(Long id) throws SQLException {
        String sql = "DELETE FROM exam_detail WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }
}
