package com.hourai.prts.entity;

/*
  考试记录模型
*/
public class ExamRecord {
    public long id;
    public long userId;
    public int score;
    public String completedAt;

    public ExamRecord(long id,long userId,int score,String completedAt){
        this.id=id;this.userId=userId;this.score=score;this.completedAt=completedAt;
    }
}