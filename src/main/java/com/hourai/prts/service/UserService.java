package com.hourai.prts.service;

import com.hourai.prts.dao.UserDao;
import com.hourai.prts.entity.User;
import java.sql.SQLException;
import java.util.List;

public class UserService {
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
