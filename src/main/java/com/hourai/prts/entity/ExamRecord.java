package com.hourai.prts.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ExamRecord {
    public ExamRecord(Long id, Long userId, int score, String completedAt) {
      this.id = id;
      this.userId = userId;
      this.score = new java.math.BigDecimal(score);
      try {
        this.createdAt = java.sql.Timestamp.valueOf(completedAt);
      } catch (Exception e) {
        this.createdAt = null;
      }
    }
  private Long id;
  private Long userId;
  private String examName;
  private int totalQuestions;
  private int correctCount;
  private BigDecimal score;
  private Integer duration;
  private Timestamp createdAt;

  public ExamRecord() {}

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }

  public String getExamName() { return examName; }
  public void setExamName(String examName) { this.examName = examName; }

  public int getTotalQuestions() { return totalQuestions; }
  public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

  public int getCorrectCount() { return correctCount; }
  public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

  public BigDecimal getScore() { return score; }
  public void setScore(BigDecimal score) { this.score = score; }

  public Integer getDuration() { return duration; }
  public void setDuration(Integer duration) { this.duration = duration; }

  public Timestamp getCreatedAt() { return createdAt; }
  public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}