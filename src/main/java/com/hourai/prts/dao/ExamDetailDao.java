package com.hourai.prts.dao;

import com.hourai.prts.entity.ExamDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDetailDao {
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
