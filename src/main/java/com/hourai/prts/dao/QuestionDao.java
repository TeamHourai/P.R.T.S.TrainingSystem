package com.hourai.prts.dao;

import com.hourai.prts.entity.Question;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public int insert(Question q) throws SQLException {
        String sql = "INSERT INTO question (type, difficulty, category, resource, question, options, answer, analysis, has_picture, picture_url, view_count, error_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, q.getType());
            ps.setInt(2, q.getDifficulty());
            ps.setString(3, q.getCategory());
            ps.setString(4, q.getResource());
            ps.setString(5, q.getQuestion());
            ps.setString(6, q.getOptions());
            ps.setString(7, q.getAnswer());
            ps.setString(8, q.getAnalysis());
            ps.setBoolean(9, q.isHasPicture());
            ps.setString(10, q.getPictureUrl());
            ps.setInt(11, q.getViewCount());
            ps.setInt(12, q.getErrorCount());
            return ps.executeUpdate();
        }
    }

    public Question selectById(Long id) throws SQLException {
        String sql = "SELECT * FROM question WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Question q = new Question();
                q.setId(rs.getLong("id"));
                q.setType(rs.getInt("type"));
                q.setDifficulty(rs.getInt("difficulty"));
                q.setCategory(rs.getString("category"));
                q.setResource(rs.getString("resource"));
                q.setQuestion(rs.getString("question"));
                q.setOptions(rs.getString("options"));
                q.setAnswer(rs.getString("answer"));
                q.setAnalysis(rs.getString("analysis"));
                q.setHasPicture(rs.getBoolean("has_picture"));
                q.setPictureUrl(rs.getString("picture_url"));
                q.setViewCount(rs.getInt("view_count"));
                q.setErrorCount(rs.getInt("error_count"));
                q.setCreatedAt(rs.getTimestamp("created_at"));
                q.setUpdatedAt(rs.getTimestamp("updated_at"));
                return q;
            }
        }
        return null;
    }

    public List<Question> selectAll() throws SQLException {
        String sql = "SELECT * FROM question";
        List<Question> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Question q = new Question();
                q.setId(rs.getLong("id"));
                q.setType(rs.getInt("type"));
                q.setDifficulty(rs.getInt("difficulty"));
                q.setCategory(rs.getString("category"));
                q.setResource(rs.getString("resource"));
                q.setQuestion(rs.getString("question"));
                q.setOptions(rs.getString("options"));
                q.setAnswer(rs.getString("answer"));
                q.setAnalysis(rs.getString("analysis"));
                q.setHasPicture(rs.getBoolean("has_picture"));
                q.setPictureUrl(rs.getString("picture_url"));
                q.setViewCount(rs.getInt("view_count"));
                q.setErrorCount(rs.getInt("error_count"));
                q.setCreatedAt(rs.getTimestamp("created_at"));
                q.setUpdatedAt(rs.getTimestamp("updated_at"));
                list.add(q);
            }
        }
        return list;
    }

    public int update(Question q) throws SQLException {
        String sql = "UPDATE question SET type=?, difficulty=?, category=?, resource=?, question=?, options=?, answer=?, analysis=?, has_picture=?, picture_url=?, view_count=?, error_count=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, q.getType());
            ps.setInt(2, q.getDifficulty());
            ps.setString(3, q.getCategory());
            ps.setString(4, q.getResource());
            ps.setString(5, q.getQuestion());
            ps.setString(6, q.getOptions());
            ps.setString(7, q.getAnswer());
            ps.setString(8, q.getAnalysis());
            ps.setBoolean(9, q.isHasPicture());
            ps.setString(10, q.getPictureUrl());
            ps.setInt(11, q.getViewCount());
            ps.setInt(12, q.getErrorCount());
            ps.setLong(13, q.getId());
            return ps.executeUpdate();
        }
    }

    public int delete(Long id) throws SQLException {
        String sql = "DELETE FROM question WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }
}
