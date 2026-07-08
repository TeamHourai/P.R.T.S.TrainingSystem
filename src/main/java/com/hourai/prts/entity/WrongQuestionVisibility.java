package com.hourai.prts.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "wrong_visibility", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"}))
public class WrongQuestionVisibility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    private Boolean hidden = false;

    @Column(name = "updated_at", length = 50)
    private String updatedAt;

    public WrongQuestionVisibility() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Boolean getHidden() { return hidden; }
    public void setHidden(Boolean hidden) { this.hidden = hidden; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
