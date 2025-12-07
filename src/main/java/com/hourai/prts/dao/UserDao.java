/**
 * 用户数据访问对象（DAO），负责对 user 表进行增删改查操作。
 */
package com.hourai.prts.dao;

import com.hourai.prts.entity.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDao 提供对用户表的数据库操作方法。
 */
public class UserDao {
    /**
     * 数据库连接 URL
     */
    private final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    /**
     * 数据库用户名
     */
    private final String user = "root";
    /**
     * 数据库密码
     */
    private final String password = "p.r.t.s.data115";

    /**
     * 新增用户记录
     * @param u 用户实体
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
    public int insert(User u) throws SQLException {
        String sql = "INSERT INTO user (username, password, nickname, avatar, email, is_admin, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getNickname());
            ps.setString(4, u.getAvatar());
            ps.setString(5, u.getEmail());
            ps.setBoolean(6, u.isAdmin());
            ps.setBoolean(7, u.isStatus());
            int result = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    u.setId(rs.getLong(1));
                }
            }
            return result;
        }
    }

    /**
     * 根据主键查询用户
     * @param id 用户ID
     * @return 用户实体或 null
     * @throws SQLException 数据库异常
     */
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

    /**
     * 查询所有用户
     * @return 用户列表
     * @throws SQLException 数据库异常
     */
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

    /**
     * 更新用户信息
     * @param u 用户实体
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
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

    /**
     * 删除用户
     * @param id 用户ID
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
    public int delete(Long id) throws SQLException {
        String sql = "DELETE FROM user WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }
}
