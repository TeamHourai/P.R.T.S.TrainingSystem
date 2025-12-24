package com.hourai.prts.entity;

public class NotificationsState {
    private Long id;
    private Long userId;
    private Long notificationId;
    private boolean read;
    private String readAt;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getNotificationId() {
        return notificationId;
    }
    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
    public boolean isRead() {
        return read;
    }
    public void setRead(boolean read) {
        this.read = read;
    }
    public String getReadAt() {
        return readAt;
    }
    public void setReadAt(String readAt) {
        this.readAt = readAt;
    }
}
