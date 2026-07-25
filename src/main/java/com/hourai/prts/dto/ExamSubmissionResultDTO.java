package com.hourai.prts.dto;

import java.math.BigDecimal;
import java.util.List;

public class ExamSubmissionResultDTO {
    private Long examId;
    private BigDecimal score;
    private Integer totalQuestions;
    private Integer correctCount;
    private List<ExamQuestionResultDTO> questions;

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }
    public List<ExamQuestionResultDTO> getQuestions() { return questions; }
    public void setQuestions(List<ExamQuestionResultDTO> questions) { this.questions = questions; }
}
