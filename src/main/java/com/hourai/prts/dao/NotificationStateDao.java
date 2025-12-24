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
        // Select columns adaptively depending on DB schema
        String readCol = DbCompat.columnExists("notifications_state", "is_read") ? "is_read"
                : (DbCompat.columnExists("notifications_state", "read") ? "read" : "is_read");
        String hiddenCol = DbCompat.columnExists("notifications_state", "is_hidden") ? "is_hidden"
                : (DbCompat.columnExists("notifications_state", "deleted") ? "deleted" : "is_hidden");
        String sql = String.format("SELECT notification_id, %s, %s FROM notifications_state WHERE user_id = ?", readCol, hiddenCol);
        List<NotificationState> out = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long nid = rs.getLong("notification_id");
                    boolean read = rs.getBoolean(readCol);
                    boolean hidden = rs.getBoolean(hiddenCol);
                    out.add(new NotificationState(userId, nid, read, hidden));
                }
            }
        }
        return out;
    }

    public int upsert(NotificationState ns) throws SQLException {
        String readCol = DbCompat.columnExists("notifications_state", "is_read") ? "is_read"
            : (DbCompat.columnExists("notifications_state", "read") ? "read" : "is_read");
        String hiddenCol = DbCompat.columnExists("notifications_state", "is_hidden") ? "is_hidden"
            : (DbCompat.columnExists("notifications_state", "deleted") ? "deleted" : "is_hidden");
        String sqlInsert = String.format("INSERT INTO notifications_state (user_id, notification_id, %s, %s) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE %s = VALUES(%s), %s = VALUES(%s)",
            readCol, hiddenCol, readCol, readCol, hiddenCol, hiddenCol);
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
