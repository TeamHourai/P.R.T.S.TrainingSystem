package com.hourai.prts.entity;

/**
 * Persisted per-user visibility state for wrong-question list.
 *
 * Contract:
 * - If a user hides a question from the wrong list, we DO NOT delete answer history.
 * - The hide state is stored separately and survives logout/login.
 */
public class WrongQuestionVisibility {
    private long id;
    private long userId;
    private long questionId;
    private boolean hidden;
    private String updatedAt;

    public WrongQuestionVisibility() {}

    public WrongQuestionVisibility(long id, long userId, long questionId, boolean hidden, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.questionId = questionId;
        this.hidden = hidden;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

