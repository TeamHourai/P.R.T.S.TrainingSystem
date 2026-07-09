package com.hourai.prts.repository;

import com.hourai.prts.entity.WrongQuestionVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WrongQuestionVisibilityRepository extends JpaRepository<WrongQuestionVisibility, Long> {
    Optional<WrongQuestionVisibility> findByUserIdAndQuestionId(Long userId, Long questionId);
    List<WrongQuestionVisibility> findByUserIdAndHiddenFalse(Long userId);
    List<WrongQuestionVisibility> findByUserIdAndHiddenTrue(Long userId);
}
