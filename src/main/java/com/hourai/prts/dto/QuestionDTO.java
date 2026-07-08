package com.hourai.prts.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionDTO {
    private Long id;
    private Integer type;
    private Integer difficulty;
    private String category;
    private String resource;
    private String question;
    private Boolean picture;
    private String pictureUrl;
    private List<String> options;
    private Integer answer;
    private String analysis;
    private List<String> keywords;
    private Integer viewCount;
    private Integer errorCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public Boolean getPicture() { return picture; }
    public void setPicture(Boolean picture) { this.picture = picture; }
    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public Integer getAnswer() { return answer; }
    public void setAnswer(Integer answer) { this.answer = answer; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getErrorCount() { return errorCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
