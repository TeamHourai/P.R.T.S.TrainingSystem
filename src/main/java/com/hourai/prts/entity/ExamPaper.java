package com.hourai.prts.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Server-side paper instance. It binds the complete issued question set to one user. */
@Entity
@Table(name = "exam_papers")
public class ExamPaper {
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_SUBMITTED = "SUBMITTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false, length = 20)
    private String status = STATUS_ACTIVE;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
