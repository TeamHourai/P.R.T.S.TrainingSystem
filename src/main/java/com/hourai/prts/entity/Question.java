package com.hourai.prts.entity;

import java.sql.Timestamp;

public class Question {
    public Question(Long id, int type, int difficulty, String resource, String question, boolean hasPicture, java.util.List<String> options, int answer, String analysis) {
      this.id = id;
      this.type = type;
      this.difficulty = difficulty;
      this.resource = resource;
      this.question = question;
      this.hasPicture = hasPicture;
      this.options = String.join("|", options);
      this.answer = String.valueOf(answer);
      this.analysis = analysis;
    }
  private Long id;
  private int type;
  private int difficulty;
  private String category;
  private String resource;
  private String question;
  private String options; // JSON 字符串
  private String answer;
  private String analysis;
  private boolean hasPicture;
  private String pictureUrl;
  private int viewCount;
  private int errorCount;
  private Timestamp createdAt;
  private Timestamp updatedAt;

  public Question() {}

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public int getType() { return type; }
  public void setType(int type) { this.type = type; }

  public int getDifficulty() { return difficulty; }
  public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }

  public String getResource() { return resource; }
  public void setResource(String resource) { this.resource = resource; }

  public String getQuestion() { return question; }
  public void setQuestion(String question) { this.question = question; }

  public String getOptions() { return options; }
  public void setOptions(String options) { this.options = options; }

  public String getAnswer() { return answer; }
  public void setAnswer(String answer) { this.answer = answer; }

  public String getAnalysis() { return analysis; }
  public void setAnalysis(String analysis) { this.analysis = analysis; }

  public boolean isHasPicture() { return hasPicture; }
  public void setHasPicture(boolean hasPicture) { this.hasPicture = hasPicture; }

  public String getPictureUrl() { return pictureUrl; }
  public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }

  public int getViewCount() { return viewCount; }
  public void setViewCount(int viewCount) { this.viewCount = viewCount; }

  public int getErrorCount() { return errorCount; }
  public void setErrorCount(int errorCount) { this.errorCount = errorCount; }

  public Timestamp getCreatedAt() { return createdAt; }
  public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

  public Timestamp getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}