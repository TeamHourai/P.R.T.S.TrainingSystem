package com.hourai.prts.dao;

import com.hourai.prts.entity.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";
    private static final String TABLE = "users";

    public int insert(User u) throws SQLException {
        boolean hasId = u.getId() != null;
        String sql = hasId ?
            "INSERT INTO users (id, username, password, is_admin, register_time) VALUES (?, ?, ?, ?, ?)" :
            "INSERT INTO users (username, password, is_admin, register_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (hasId) {
                ps.setLong(idx++, u.getId());
            }
            ps.setString(idx++, u.getUsername());
            ps.setString(idx++, u.getPassword());
            ps.setBoolean(idx++, u.isAdmin());
            ps.setTimestamp(idx, u.getCreatedAt() == null ? new Timestamp(System.currentTimeMillis()) : u.getCreatedAt());
            return ps.executeUpdate();
        }
    }

    public User selectById(Long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                // Admin column compatibility
                String adminCol = pickColumn("is_admin", "admin", "isAdmin");
                if (adminCol != null) {
                    u.setAdmin(rs.getBoolean(adminCol));
                } else {
                    u.setAdmin(false);
                }

                // Created time compatibility
                String timeCol = pickColumn("register_time", "created_at", "createdAt");
                if (timeCol != null) {
                    try {
                        Timestamp ts = rs.getTimestamp(timeCol);
                        u.setCreatedAt(ts);
                    } catch (SQLException ignored) {
                        u.setCreatedAt(null);
                    }
                } else {
                    u.setCreatedAt(null);
                }
                return u;
            }
        }
        return null;
    }

    public List<User> selectAll() throws SQLException {
        String sql = "SELECT * FROM users";
        List<User> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                String adminCol = pickColumn("is_admin", "admin", "isAdmin");
                if (adminCol != null) {
                    u.setAdmin(rs.getBoolean(adminCol));
                } else {
                    u.setAdmin(false);
                }

                String timeCol = pickColumn("register_time", "created_at", "createdAt");
                if (timeCol != null) {
                    try {
                        Timestamp ts = rs.getTimestamp(timeCol);
                        u.setCreatedAt(ts);
                    } catch (SQLException ignored) {
                        u.setCreatedAt(null);
                    }
                } else {
                    u.setCreatedAt(null);
                }
                list.add(u);
            }
        }
        return list;
    }

    // helper: pick first existing column name using metadata checks
    private String pickColumn(String... candidates) {
        for (String c : candidates) {
            if (c == null) continue;
            try {
                if (DbCompat.columnExists(TABLE, c)) return c;
            } catch (Exception ignored) {}
            try {
                if (DbCompat.columnExists(TABLE, c.toLowerCase())) return c.toLowerCase();
            } catch (Exception ignored) {}
            try {
                if (DbCompat.columnExists(TABLE, c.toUpperCase())) return c.toUpperCase();
            } catch (Exception ignored) {}
        }
        return null;
    }

    public int update(User u) throws SQLException {
        String sql = "UPDATE users SET password=?, is_admin=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getPassword());
            ps.setBoolean(2, u.isAdmin());
            ps.setLong(3, u.getId());
            return ps.executeUpdate();
        }
    }

    public int delete(Long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }
}
