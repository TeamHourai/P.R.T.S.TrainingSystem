package com.hourai.prts.dto;

import java.util.List;

/** Generated exam paper plus the server-side snapshot identifier used at submission. */
public class ExamPaperDTO {
    private Long paperId;
    private List<ExamPaperQuestionDTO> questions;

    public ExamPaperDTO() {}

    public ExamPaperDTO(Long paperId, List<ExamPaperQuestionDTO> questions) {
        this.paperId = paperId;
        this.questions = questions;
    }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }
    public List<ExamPaperQuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<ExamPaperQuestionDTO> questions) { this.questions = questions; }
}
