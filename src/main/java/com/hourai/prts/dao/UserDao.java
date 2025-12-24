package com.hourai.prts.dao;

import com.hourai.prts.entity.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = "p.r.t.s.data115";

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
                u.setAdmin(rs.getBoolean("is_admin"));
                Timestamp ts = rs.getTimestamp("register_time");
                u.setCreatedAt(ts);
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
                u.setAdmin(rs.getBoolean("is_admin"));
                Timestamp ts = rs.getTimestamp("register_time");
                u.setCreatedAt(ts);
                list.add(u);
            }
        }
        return list;
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
