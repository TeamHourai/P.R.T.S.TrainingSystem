package com.hourai.prts.dto;

/**
 * 交卷后的单题判分结果。
 *
 * <p>答案与解析只在服务器完成判分后返回。
 */
public class ExamQuestionResultDTO {
    private Long id;
    private Integer selectedAnswer;
    private Integer answer;
    private Boolean correct;
    private String analysis;

    public ExamQuestionResultDTO() {}

    public ExamQuestionResultDTO(Long id, Integer selectedAnswer, Integer answer,
                                 Boolean correct, String analysis) {
        this.id = id;
        this.selectedAnswer = selectedAnswer;
        this.answer = answer;
        this.correct = correct;
        this.analysis = analysis;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getSelectedAnswer() { return selectedAnswer; }
    public void setSelectedAnswer(Integer selectedAnswer) { this.selectedAnswer = selectedAnswer; }
    public Integer getAnswer() { return answer; }
    public void setAnswer(Integer answer) { this.answer = answer; }
    public Boolean getCorrect() { return correct; }
    public void setCorrect(Boolean correct) { this.correct = correct; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
}
