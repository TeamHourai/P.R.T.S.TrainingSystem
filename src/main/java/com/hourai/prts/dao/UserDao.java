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
        String sql = "INSERT INTO user (username, password, nickname, avatar, email, is_admin, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getNickname());
            ps.setString(4, u.getAvatar());
            ps.setString(5, u.getEmail());
            ps.setBoolean(6, u.isAdmin());
            ps.setBoolean(7, u.isStatus());
            return ps.executeUpdate();
        }
    }

    public User selectById(Long id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setNickname(rs.getString("nickname"));
                u.setAvatar(rs.getString("avatar"));
                u.setEmail(rs.getString("email"));
                u.setAdmin(rs.getBoolean("is_admin"));
                u.setStatus(rs.getBoolean("status"));
                // u.setCreatedAt(rs.getTimestamp("created_at"));
                // u.setUpdatedAt(rs.getTimestamp("updated_at"));
                return u;
            }
        }
        return null;
    }

    public List<User> selectAll() throws SQLException {
        String sql = "SELECT * FROM user";
        List<User> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setNickname(rs.getString("nickname"));
                u.setAvatar(rs.getString("avatar"));
                u.setEmail(rs.getString("email"));
                u.setAdmin(rs.getBoolean("is_admin"));
                u.setStatus(rs.getBoolean("status"));
                // u.setCreatedAt(rs.getTimestamp("created_at"));
                // u.setUpdatedAt(rs.getTimestamp("updated_at"));
                list.add(u);
            }
        }
        return list;
    }

    public int update(User u) throws SQLException {
        String sql = "UPDATE user SET password=?, nickname=?, avatar=?, email=?, is_admin=?, status=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getPassword());
            ps.setString(2, u.getNickname());
            ps.setString(3, u.getAvatar());
            ps.setString(4, u.getEmail());
            ps.setBoolean(5, u.isAdmin());
            ps.setBoolean(6, u.isStatus());
            ps.setLong(7, u.getId());
            return ps.executeUpdate();
        }
    }

    public int delete(Long id) throws SQLException {
        String sql = "DELETE FROM user WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }
}
