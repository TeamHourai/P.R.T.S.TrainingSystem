package com.hourai.prts.repository;

import com.hourai.prts.entity.ExamPaperQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamPaperQuestionRepository extends JpaRepository<ExamPaperQuestion, Long> {
    List<ExamPaperQuestion> findByPaperIdOrderByPositionAsc(Long paperId);
}
