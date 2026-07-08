package com.hourai.prts.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "answer_settings")
public class AnswerSettings {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "auto_submit")
    private Boolean autoSubmit = false;

    @Column(name = "auto_next_correct")
    private Boolean autoNextCorrect = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AnswerSettings() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Boolean getAutoSubmit() { return autoSubmit; }
    public void setAutoSubmit(Boolean autoSubmit) { this.autoSubmit = autoSubmit; }
    public Boolean getAutoNextCorrect() { return autoNextCorrect; }
    public void setAutoNextCorrect(Boolean autoNextCorrect) { this.autoNextCorrect = autoNextCorrect; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
