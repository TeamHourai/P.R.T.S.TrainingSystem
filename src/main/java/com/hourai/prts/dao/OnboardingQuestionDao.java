package com.hourai.prts.dao;

import com.hourai.prts.entity.OnboardingQuestion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OnboardingQuestionDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public List<OnboardingQuestion> selectAll() throws SQLException {
        String sql = "SELECT * FROM questions_onboarding";
        List<OnboardingQuestion> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                OnboardingQuestion q = mapRow(rs);
                list.add(q);
            }
        }
        return list;
    }

    public List<OnboardingQuestion> selectByGroupId(int groupId) throws SQLException {
        String sql = "SELECT * FROM questions_onboarding WHERE group_id = ?";
        List<OnboardingQuestion> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public OnboardingQuestion selectById(int id) throws SQLException {
        String sql = "SELECT * FROM questions_onboarding WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public int insert(OnboardingQuestion q) throws SQLException {
        String sql = "INSERT INTO questions_onboarding (id, group_id, type_id, image_url, question, is_multi, options, answer, analysis) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, q.getId() == null ? 0 : q.getId());
            if (q.getGroupId() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, q.getGroupId());
            if (q.getTypeId() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, q.getTypeId());
            ps.setString(4, q.getImageUrl());
            ps.setString(5, q.getQuestion());
            if (q.getIsMulti() == null) ps.setNull(6, Types.TINYINT); else ps.setBoolean(6, q.getIsMulti());
            ps.setString(7, q.getOptions());
            ps.setString(8, q.getAnswer());
            ps.setString(9, q.getAnalysis());
            return ps.executeUpdate();
        }
    }

    public int update(OnboardingQuestion q) throws SQLException {
        String sql = "UPDATE questions_onboarding SET group_id = ?, type_id = ?, image_url = ?, question = ?, is_multi = ?, options = ?, answer = ?, analysis = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (q.getGroupId() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, q.getGroupId());
            if (q.getTypeId() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, q.getTypeId());
            ps.setString(3, q.getImageUrl());
            ps.setString(4, q.getQuestion());
            if (q.getIsMulti() == null) ps.setNull(5, Types.TINYINT); else ps.setBoolean(5, q.getIsMulti());
            ps.setString(6, q.getOptions());
            ps.setString(7, q.getAnswer());
            ps.setString(8, q.getAnalysis());
            ps.setInt(9, q.getId() == null ? 0 : q.getId());
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        String sql = "DELETE FROM questions_onboarding WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private OnboardingQuestion mapRow(ResultSet rs) throws SQLException {
        OnboardingQuestion q = new OnboardingQuestion();
        q.setId(rs.getInt("id"));
        int gid = rs.getInt("group_id"); if (rs.wasNull()) q.setGroupId(null); else q.setGroupId(gid);
        int tid = rs.getInt("type_id"); if (rs.wasNull()) q.setTypeId(null); else q.setTypeId(tid);
        q.setImageUrl(rs.getString("image_url"));
        q.setQuestion(rs.getString("question"));
        q.setIsMulti(rs.getBoolean("is_multi"));
        q.setOptions(rs.getString("options"));
        q.setAnswer(rs.getString("answer"));
        q.setAnalysis(rs.getString("analysis"));
        return q;
    }
}
