package com.hourai.prts.repository;

import com.hourai.prts.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    List<UserAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);
    List<UserAnswer> findByUserIdAndIsCorrectFalse(Long userId);
    long countByQuestionId(Long questionId);
    long countByQuestionIdAndIsCorrectTrue(Long questionId);
    List<UserAnswer> findByQuestionIdAndSelectedAnswer(Long questionId, String selectedAnswer);
}
