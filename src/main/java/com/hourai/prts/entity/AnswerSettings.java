package com.hourai.prts.entity;

public class AnswerSettings {
    public boolean autoSubmit;
    public boolean autoNextCorrect;

    public AnswerSettings() {}

    public AnswerSettings(boolean autoSubmit, boolean autoNextCorrect) {
        this.autoSubmit = autoSubmit;
        this.autoNextCorrect = autoNextCorrect;
    }
}
