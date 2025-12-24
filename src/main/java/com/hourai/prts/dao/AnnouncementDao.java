package com.hourai.prts.dao;

import com.hourai.prts.entity.Announcement;
import java.sql.*;

public class AnnouncementDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public int insert(Announcement a) throws SQLException {
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
