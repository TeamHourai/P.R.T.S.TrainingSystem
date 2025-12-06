package com.hourai.prts.entity;

import java.sql.Timestamp;

public class User {
    public User(Long id, String username, String password, boolean isAdmin, String createdAt) {
      this.id = id;
      this.username = username;
      this.password = password;
      this.isAdmin = isAdmin;
      // createdAt 字段类型为 Timestamp，需转换
      try {
        this.createdAt = java.sql.Timestamp.valueOf(createdAt);
      } catch (Exception e) {
        this.createdAt = null;
      }
    }
  private Long id;
  private String username;
  private String password;
  private String nickname;
  private String avatar;
  private String email;
  private boolean isAdmin;
  private boolean status;
  private Timestamp createdAt;
  private Timestamp updatedAt;

  public User() {}

  // getter/setter
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public String getNickname() { return nickname; }
  public void setNickname(String nickname) { this.nickname = nickname; }

  public String getAvatar() { return avatar; }
  public void setAvatar(String avatar) { this.avatar = avatar; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public boolean isAdmin() { return isAdmin; }
  public void setAdmin(boolean admin) { isAdmin = admin; }

  public boolean isStatus() { return status; }
  public void setStatus(boolean status) { this.status = status; }

  public Timestamp getCreatedAt() { return createdAt; }
  public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

  public Timestamp getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}