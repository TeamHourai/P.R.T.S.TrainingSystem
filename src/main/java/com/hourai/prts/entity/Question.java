package com.hourai.prts.entity;

import java.util.List;

/*
  题目模型
*/
public class Question {
    public long id;
    /*type各值含义：
     *1: '干员调配与特性化决策',
     *2: '空间部署与极致化战术',
     *3: '效能审计与生态位界定',
     *4: '横向分析与竞争力评估',
     *5: '作战环境与档案类记录',
     */
    public int type;
    /*difficulty各值含义：
     *1: '常识',
     *2: '基操',
     *3: '娴熟',
     *4: '明智',
     *5: '深邃',
     */
    public int difficulty;
    /*resource字段含义：题目出处。字符串，管理员在添加时填写*/
    public String resource;
    /*question字段含义：题干文本内容*/
    public String question;
    public boolean hasPicture;
    /*options字段含义：选项文本内容列表，按顺序存储*/
    public List<String> options;
    /*answer字段含义：正确选项的序号*/
    public int answer; // 1-based
    /*analysis字段含义：答案解析文本内容*/
    public String analysis;

    public Question(long id, int type, int difficulty, String resource, String question, boolean hasPicture, java.util.List<String> options, int answer, String analysis) {
        this.id = id;
        this.type = type;
        this.difficulty = difficulty;
        this.resource = resource;
        this.question = question;
        this.hasPicture = hasPicture;
        this.options = options;
        this.answer = answer;
        this.analysis = analysis;
    }
}