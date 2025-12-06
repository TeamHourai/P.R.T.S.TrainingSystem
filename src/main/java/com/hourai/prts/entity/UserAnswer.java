package com.hourai.prts.entity;

/*
  用户答题记录模型
*/
public class UserAnswer {
    public long id;
    public long userId;
    public long questionId;
    /*questionType字段含义：
     *'dispatch': '干员调配与特性化决策',
     *'deployment': '空间部署与极致化战术',
     *'efficiency': '效能审计与生态位界定',
     *'analysis': '横向分析与竞争力评估',
     *'environment': '作战环境与档案类记录',
     */
    public String questionType;
    public boolean isCorrect;
    public int selectedOption;
    public String answeredAt;

    public UserAnswer(long id, long userId, long questionId, String questionType, boolean isCorrect, int selectedOption, String answeredAt) {
        this.id = id;
        this.userId = userId;
        this.questionId = questionId;
        this.questionType = questionType;
        this.isCorrect = isCorrect;
        this.selectedOption = selectedOption;
        this.answeredAt = answeredAt;
    }
}