package com.hourai.prts.repository;

import com.hourai.prts.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    Page<Question> findByType(Integer type, Pageable pageable);
    Page<Question> findByDifficulty(Integer difficulty, Pageable pageable);
    Page<Question> findByTypeAndDifficulty(Integer type, Integer difficulty, Pageable pageable);
}
