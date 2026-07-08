package com.hourai.prts.repository;

import com.hourai.prts.entity.OnboardingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnboardingQuestionRepository extends JpaRepository<OnboardingQuestion, Integer> {
    List<OnboardingQuestion> findByGroupId(Integer groupId);
}
