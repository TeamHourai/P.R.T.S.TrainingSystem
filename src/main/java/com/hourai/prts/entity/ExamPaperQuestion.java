package com.hourai.prts.entity;

import jakarta.persistence.*;

/** One question in an issued paper snapshot. */
@Entity
@Table(name = "exam_paper_questions")
public class ExamPaperQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "paper_id", nullable = false)
    private Long paperId;
    @Column(name = "question_id", nullable = false)
    private Long questionId;
    @Column(nullable = false)
    private Integer position;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
