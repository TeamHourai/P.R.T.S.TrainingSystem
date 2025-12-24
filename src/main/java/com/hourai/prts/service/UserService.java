package com.hourai.prts.service;

import com.hourai.prts.dao.UserDao;
import com.hourai.prts.entity.User;
import java.sql.SQLException;
import java.util.List;

public class UserService {
        // 检查用户名是否存在
        public boolean usernameExists(String username) throws SQLException {
            List<User> users = userDao.selectAll();
            return users.stream().anyMatch(u -> u.getUsername() != null && u.getUsername().equals(username));
        }

        // 获取下一个用户id（最大id+1）
        public long getNextUserId() throws SQLException {
            List<User> users = userDao.selectAll();
            return users.stream().mapToLong(u -> u.getId() == null ? 0 : u.getId()).max().orElse(0L) + 1;
        }
    private final UserDao userDao = new UserDao();

    public int register(User user) throws SQLException {
        // 业务逻辑：如用户名唯一性校验等可在此扩展
        return userDao.insert(user);
    }

    public User getUserById(Long id) throws SQLException {
        return userDao.selectById(id);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDao.selectAll();
    }

    public int updateUser(User user) throws SQLException {
        return userDao.update(user);
    }

    public int deleteUser(Long id) throws SQLException {
        return userDao.delete(id);
    }
}
