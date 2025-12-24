package com.hourai.prts.dao;

import com.hourai.prts.entity.NotificationState;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationStateDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

    public List<NotificationState> selectByUserId(long userId) throws SQLException {
        String sql = "SELECT notification_id, is_read, is_hidden FROM notifications_state WHERE user_id = ?";
        List<NotificationState> out = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long nid = rs.getLong("notification_id");
                    boolean read = rs.getBoolean("is_read");
                    boolean hidden = rs.getBoolean("is_hidden");
                    out.add(new NotificationState(userId, nid, read, hidden));
                }
            }
        }
        return out;
    }

    public int upsert(NotificationState ns) throws SQLException {
        String sqlInsert = "INSERT INTO notifications_state (user_id, notification_id, is_read, is_hidden) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE is_read = VALUES(is_read), is_hidden = VALUES(is_hidden)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setLong(1, ns.getUserId());
            ps.setLong(2, ns.getNotificationId());
            ps.setBoolean(3, ns.isRead());
            ps.setBoolean(4, ns.isHidden());
            return ps.executeUpdate();
        }
    }
}
