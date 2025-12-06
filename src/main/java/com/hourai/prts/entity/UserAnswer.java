package com.hourai.prts.entity;

/*
  用户答题记录模型
*/
public class UserAnswer {
    public long id;
    public long userId;
    public long questionId;
    public String questionType;
    public boolean isCorrect;
    public int selectedOption;
    public String answeredAt;

    public UserAnswer(long id,long userId,long questionId,String questionType,boolean isCorrect,int selectedOption,String answeredAt){
        this.id=id;this.userId=userId;this.questionId=questionId;this.questionType=questionType;this.isCorrect=isCorrect;this.selectedOption=selectedOption;this.answeredAt=answeredAt;
    }
}