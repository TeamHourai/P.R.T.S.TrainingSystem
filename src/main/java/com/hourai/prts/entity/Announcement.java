package com.hourai.prts.entity;

/**
 * System announcement (server-side).
 *
 * Stored in CSV for now (future DB migration friendly).
 */
public class Announcement {
    private Long id;
    // Notification type: system/exam/answer/warning/update/reward
    private String type = "system";
    private String title;
    private String content;
    private boolean important;
    private String createdAt;
    private String createdBy;
    private String expiresAt;

    public Announcement() {}

    public Announcement(Long id, String title, String content, boolean important, String createdAt, String createdBy, String expiresAt) {
        this.id = id;
        this.type = "system";
        this.title = title;
        this.content = content;
        this.important = important;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.expiresAt = expiresAt;
    }

    public Announcement(Long id, String type, String title, String content, boolean important, String createdAt, String createdBy, String expiresAt) {
        this.id = id;
        this.type = (type == null || type.trim().isEmpty()) ? "system" : type.trim();
        this.title = title;
        this.content = content;
        this.important = important;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = (type == null || type.trim().isEmpty()) ? "system" : type.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}
