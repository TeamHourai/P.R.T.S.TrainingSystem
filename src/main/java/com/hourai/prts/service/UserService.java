package com.hourai.prts.service;

import com.hourai.prts.entity.User;
import com.hourai.prts.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户注册、密码验证和管理员权限变更的业务服务。
 *
 * <p>所有密码在持久化前使用 PasswordEncoder 单向哈希；权限变更的操作者 ID
 * 必须来自已经验证的 Spring Security principal。
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public User register(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("username exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setIsAdmin(false);
        user.setStatus(true);
        user.setRegisterTime(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Transactional
    public boolean setAdminStatus(Long actorId, Long targetId, boolean makeAdmin) {
        User actor = userRepository.findById(actorId).orElseThrow();
        if (!actor.getIsAdmin()) throw new RuntimeException("not admin");
        User target = userRepository.findById(targetId).orElseThrow();
        // 超级管理员固定为 id=1；普通管理员可以提升用户，但不能降级管理员。
        if (!makeAdmin && !actorId.equals(1L)) throw new RuntimeException("only super admin can demote");
        if (target.getIsAdmin() == makeAdmin) return false;
        target.setIsAdmin(makeAdmin);
        userRepository.save(target);
        return true;
    }

    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return userRepository.findAll();
        return userRepository.findByUsernameContainingIgnoreCase(keyword.trim());
    }
}
