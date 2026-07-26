package com.hourai.prts.repository;

import com.hourai.prts.entity.ExamPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExamPaperRepository extends JpaRepository<ExamPaper, Long> {
    Optional<ExamPaper> findByIdAndUserId(Long id, Long userId);
}
