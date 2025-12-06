package com.hourai.prts.entity;

/*
  用户模型
*/
public class User {
    public long id;
    public String username;
    public String password; // 明文，仅示例
    public boolean isAdmin;
    public String createdAt;

    public User(long id, String username, String password, boolean isAdmin, String createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.createdAt = createdAt;
    }
}