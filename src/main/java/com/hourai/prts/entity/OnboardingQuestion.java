package com.hourai.prts.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "questions_onboarding")
public class OnboardingQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "is_multi")
    private Boolean isMulti = false;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String options;

    @Column(nullable = false, length = 255)
    private String answer;

    @Column(columnDefinition = "TEXT")
    private String analysis;

    public OnboardingQuestion() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public Boolean getIsMulti() { return isMulti; }
    public void setIsMulti(Boolean isMulti) { this.isMulti = isMulti; }
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
}
