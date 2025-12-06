package com.hourai.prts.entity;

import java.sql.Timestamp;

public class UserAnswer {
    public UserAnswer(Long id, Long userId, Long questionId, String questionType, boolean isCorrect, int selected, String answeredAt) {
      this.id = id;
      this.userId = userId;
      this.questionId = questionId;
      this.selectedAnswer = String.valueOf(selected);
      this.isCorrect = isCorrect;
      try {
        this.createdAt = java.sql.Timestamp.valueOf(answeredAt);
      } catch (Exception e) {
        this.createdAt = null;
      }
    }
  private Long id;
  private Long userId;
  private Long questionId;
  private String selectedAnswer;
  private boolean isCorrect;
  private Integer answerTime;
  private Timestamp createdAt;

  public UserAnswer() {}

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }

  public Long getQuestionId() { return questionId; }
  public void setQuestionId(Long questionId) { this.questionId = questionId; }

  public String getSelectedAnswer() { return selectedAnswer; }
  public void setSelectedAnswer(String selectedAnswer) { this.selectedAnswer = selectedAnswer; }

  public boolean isCorrect() { return isCorrect; }
  public void setCorrect(boolean correct) { isCorrect = correct; }

  public Integer getAnswerTime() { return answerTime; }
  public void setAnswerTime(Integer answerTime) { this.answerTime = answerTime; }

  public Timestamp getCreatedAt() { return createdAt; }
  public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}