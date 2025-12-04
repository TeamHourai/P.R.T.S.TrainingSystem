package com.hourai.prts.entity;

/*
  记录【全真模拟】板块的考试结果
*/
public class ExamRecord {
    public long id;
    public long userId;
    /*score字段含义：用户【全真模拟】得分*/
    public int score;
    /*completedAt字段含义：用户完成考试的时刻*/
    public String completedAt;

    public ExamRecord(long id, long userId, int score, String completedAt) {
        this.id = id;
        this.userId = userId;
        this.score = score;
        this.completedAt = completedAt;
    }
}