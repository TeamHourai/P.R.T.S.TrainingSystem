package com.hourai.prts.entity;

public class OnboardingQuestion {
    private Integer id;
    private Integer groupId;
    private Integer typeId;
    private String imageUrl;
    private String question;
    private Boolean isMulti;
    private String options;
    private String answer;
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
