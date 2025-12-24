package com.hourai.prts.dao;

import com.hourai.prts.entity.NotificationsState;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationsStateDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public int insert(NotificationsState ns) throws SQLException {
        String sql = "INSERT INTO notifications_state (user_id, notification_id, read, read_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ns.getUserId());
            ps.setLong(2, ns.getNotificationId());
            ps.setBoolean(3, ns.isRead());
            ps.setString(4, ns.getReadAt());
            return ps.executeUpdate();
        }
    }

    public NotificationsState selectById(Long id) throws SQLException {
        String sql = "SELECT * FROM notifications_state WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                NotificationsState ns = new NotificationsState();
                ns.setId(rs.getLong("id"));
                ns.setUserId(rs.getLong("user_id"));
                ns.setNotificationId(rs.getLong("notification_id"));
                ns.setRead(rs.getBoolean("read"));
                ns.setReadAt(rs.getString("read_at"));
                return ns;
            }
        }
        return null;
    }

    public List<NotificationsState> selectAll() throws SQLException {
        String sql = "SELECT * FROM notifications_state";
        List<NotificationsState> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                NotificationsState ns = new NotificationsState();
                ns.setId(rs.getLong("id"));
                ns.setUserId(rs.getLong("user_id"));
                ns.setNotificationId(rs.getLong("notification_id"));
                ns.setRead(rs.getBoolean("read"));
                ns.setReadAt(rs.getString("read_at"));
                list.add(ns);
            }
        }
        return list;
    }

    public int updateReadState(Long id, boolean read, String readAt) throws SQLException {
        String sql = "UPDATE notifications_state SET read = ?, read_at = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, read);
            ps.setString(2, readAt);
            ps.setLong(3, id);
            return ps.executeUpdate();
        }
    }

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM notifications_state WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }
}
