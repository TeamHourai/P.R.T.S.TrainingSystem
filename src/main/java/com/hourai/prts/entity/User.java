package com.hourai.prts.entity;

/*
  简单模型类，字段设为 public 方便多文件访问（教学/演示）
*/
public class User {
    public long id;
    public String username;
    public String password; // 明文，仅示例
    public boolean isAdmin;
    public String createdAt;

    public User(long id, String username, String password, boolean isAdmin, String createdAt) {
        this.id = id; this.username = username; this.password = password; this.isAdmin = isAdmin; this.createdAt = createdAt;
    }
}