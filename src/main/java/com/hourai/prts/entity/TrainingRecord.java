package com.hourai.prts.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "training_records")
@IdClass(TrainingRecord.TrainingRecordId.class)
public class TrainingRecord {
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    private Integer attempts = 0;

    private Boolean correct = false;

    @Column(name = "last_at")
    private Long lastAt = 0L;

    public TrainingRecord() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public Boolean getCorrect() { return correct; }
    public void setCorrect(Boolean correct) { this.correct = correct; }
    public Long getLastAt() { return lastAt; }
    public void setLastAt(Long lastAt) { this.lastAt = lastAt; }

    public static class TrainingRecordId implements Serializable {
        private Long userId;
        private Long questionId;

        public TrainingRecordId() {}
        public TrainingRecordId(Long userId, Long questionId) {
            this.userId = userId;
            this.questionId = questionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TrainingRecordId)) return false;
            TrainingRecordId that = (TrainingRecordId) o;
            return Objects.equals(userId, that.userId) && Objects.equals(questionId, that.questionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, questionId);
        }
    }
}
