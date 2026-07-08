package com.hourai.prts.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications_state", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "notification_id"}))
public class NotificationState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Column(name = "read_at", length = 50)
    private String readAt;

    public NotificationState() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public Boolean getIsHidden() { return isHidden; }
    public void setIsHidden(Boolean isHidden) { this.isHidden = isHidden; }
    public String getReadAt() { return readAt; }
    public void setReadAt(String readAt) { this.readAt = readAt; }
}
