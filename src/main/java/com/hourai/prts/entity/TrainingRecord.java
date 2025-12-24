package com.hourai.prts.entity;

public class TrainingRecord {
    public long questionId;
    public int attempts;
    public boolean correct;
    public long lastAt;

    public TrainingRecord() {}

    public TrainingRecord(long questionId, int attempts, boolean correct, long lastAt) {
        this.questionId = questionId;
        this.attempts = attempts;
        this.correct = correct;
        this.lastAt = lastAt;
    }
}
