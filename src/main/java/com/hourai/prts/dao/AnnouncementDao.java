package com.hourai.prts.dao;

import com.hourai.prts.entity.Announcement;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class AnnouncementDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public int insert(Announcement a) throws SQLException {
        // If id is null or <=0, let the database generate an auto-increment id (if table supports it).
        if (a.getId() == null || a.getId() <= 0) {
            String sql = "INSERT INTO announcements (type, title, content, important, created_at, created_by, expires_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, a.getType());
                ps.setString(2, a.getTitle());
                ps.setString(3, a.getContent());
                ps.setBoolean(4, a.isImportant());
                ps.setString(5, a.getCreatedAt());
                ps.setString(6, a.getCreatedBy());
                ps.setString(7, a.getExpiresAt());
                try {
                    int affected = ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            long gid = keys.getLong(1);
                            a.setId(gid);
                        }
                    }
                    return affected;
                } catch (SQLException ex) {
                    // Fallback: some schemas don't have AUTO_INCREMENT on id and reject insert without id.
                    String msg = ex.getMessage() == null ? "" : ex.getMessage();
                    if (msg.contains("Field 'id' doesn't have a default value") || msg.contains("cannot be null") || msg.contains("doesn't have a default value")) {
                        // compute next id and insert with explicit id
                        String nextSql = "SELECT COALESCE(MAX(id),0)+1 AS nextid FROM announcements";
                        long nextId = 1L;
                        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(nextSql)) {
                            if (rs.next()) nextId = rs.getLong("nextid");
                        }
                        a.setId(nextId);
                        String sql2 = "INSERT INTO announcements (id, type, title, content, important, created_at, created_by, expires_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                            ps2.setLong(1, a.getId());
                            ps2.setString(2, a.getType());
                            ps2.setString(3, a.getTitle());
                            ps2.setString(4, a.getContent());
                            ps2.setBoolean(5, a.isImportant());
                            ps2.setString(6, a.getCreatedAt());
                            ps2.setString(7, a.getCreatedBy());
                            ps2.setString(8, a.getExpiresAt());
                            return ps2.executeUpdate();
                        }
                    }
                    throw ex;
                }
            }
        } else {
            String sql = "INSERT INTO announcements (id, type, title, content, important, created_at, created_by, expires_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, a.getId());
                ps.setString(2, a.getType());
                ps.setString(3, a.getTitle());
                ps.setString(4, a.getContent());
                ps.setBoolean(5, a.isImportant());
                ps.setString(6, a.getCreatedAt());
                ps.setString(7, a.getCreatedBy());
                ps.setString(8, a.getExpiresAt());
                return ps.executeUpdate();
            }
        }
    }
    public List<Announcement> selectAll() throws SQLException {
        String sql = "SELECT * FROM announcements";
        List<Announcement> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Announcement a = new Announcement();
                a.setId(rs.getLong("id"));
                a.setType(rs.getString("type"));
                a.setTitle(rs.getString("title"));
                a.setContent(rs.getString("content"));
                a.setImportant(rs.getBoolean("important"));
                a.setCreatedAt(rs.getString("created_at"));
                a.setCreatedBy(rs.getString("created_by"));
                a.setExpiresAt(rs.getString("expires_at"));
                list.add(a);
            }
        }
        return list;
    }
}
