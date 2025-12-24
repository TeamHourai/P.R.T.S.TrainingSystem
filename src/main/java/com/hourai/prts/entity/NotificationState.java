package com.hourai.prts.entity;

public class NotificationState {
    private long userId;
    private long notificationId;
    private boolean read;
    private boolean hidden;

    public NotificationState() {}

    public NotificationState(long userId, long notificationId, boolean read, boolean hidden) {
        this.userId = userId;
        this.notificationId = notificationId;
        this.read = read;
        this.hidden = hidden;
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getNotificationId() { return notificationId; }
    public void setNotificationId(long notificationId) { this.notificationId = notificationId; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
}
