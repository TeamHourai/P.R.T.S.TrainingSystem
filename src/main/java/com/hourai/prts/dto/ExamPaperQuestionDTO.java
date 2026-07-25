package com.hourai.prts.dto;

import java.util.List;

/**
 * 正式考试发卷 DTO。
 *
 * <p>只包含答题所需字段，刻意不暴露正确答案和解析。
 */
public class ExamPaperQuestionDTO {
    private Long id;
    private Integer type;
    private Integer difficulty;
    private String category;
    private String resource;
    private String question;
    private Boolean picture;
    private String pictureUrl;
    private List<String> options;

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
}
