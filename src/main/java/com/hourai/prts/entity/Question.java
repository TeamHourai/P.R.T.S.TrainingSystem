package com.hourai.prts.entity;

import java.util.List;

/*
  题目模型
*/
public class Question {
    public long id;
    public int type;
    public int difficulty;
    public String resource;
    public String question;
    public boolean hasPicture;
    public List<String> options;
    public int answer; // 1-based
    public String analysis;

    public Question(long id,int type,int difficulty,String resource,String question,boolean hasPicture,java.util.List<String> options,int answer,String analysis){
        this.id=id;this.type=type;this.difficulty=difficulty;this.resource=resource;this.question=question;
        this.hasPicture=hasPicture;this.options=options;this.answer=answer;this.analysis=analysis;
    }
}