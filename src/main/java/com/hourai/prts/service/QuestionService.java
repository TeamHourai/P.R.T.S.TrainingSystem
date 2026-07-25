package com.hourai.prts.service;

import com.hourai.prts.dto.QuestionDTO;
import com.hourai.prts.dto.ExamPaperQuestionDTO;
import com.hourai.prts.entity.OnboardingQuestion;
import com.hourai.prts.entity.Question;
import com.hourai.prts.repository.OnboardingQuestionRepository;
import com.hourai.prts.repository.QuestionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 正式题库与入职培训题库的查询、维护和 DTO 转换服务。
 *
 * <p>正式考试必须使用 {@link ExamPaperQuestionDTO}，避免在交卷前泄露答案；
 * 练习和管理场景才使用包含答案与解析的 {@link QuestionDTO}。
 */
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final OnboardingQuestionRepository onboardingRepository;

    public QuestionService(QuestionRepository questionRepository, OnboardingQuestionRepository onboardingRepository) {
        this.questionRepository = questionRepository;
        this.onboardingRepository = onboardingRepository;
    }

    // ===== Questions =====
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    public List<Question> getFilteredQuestions(Integer type, Integer difficulty, String keyword) {
        List<Question> all = questionRepository.findAll();
        return all.stream()
            .filter(q -> type == null || q.getType().equals(type))
            .filter(q -> difficulty == null || q.getDifficulty().equals(difficulty))
            .filter(q -> {
                if (keyword == null || keyword.isEmpty()) return true;
                String kl = keyword.toLowerCase();
                // Search by ID
                try {
                    if (q.getId().equals(Long.parseLong(keyword))) return true;
                } catch (NumberFormatException ignored) {}
                if (q.getKeywords() != null && q.getKeywords().toLowerCase().contains(kl)) return true;
                return q.getQuestion() != null && q.getQuestion().toLowerCase().contains(kl);
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public Question addQuestion(Question q) {
        q.setCreatedAt(LocalDateTime.now());
        return questionRepository.save(q);
    }

    @Transactional
    public Question updateQuestion(Long id, Question updated) {
        Question q = questionRepository.findById(id).orElseThrow();
        q.setType(updated.getType());
        q.setDifficulty(updated.getDifficulty());
        q.setCategory(updated.getCategory());
        q.setResource(updated.getResource());
        q.setQuestion(updated.getQuestion());
        q.setOptions(updated.getOptions());
        q.setAnswer(updated.getAnswer());
        q.setAnalysis(updated.getAnalysis());
        q.setHasPicture(updated.getHasPicture());
        q.setPictureUrl(updated.getPictureUrl());
        q.setKeywords(updated.getKeywords());
        q.setUpdatedAt(LocalDateTime.now());
        return questionRepository.save(q);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    @Transactional
    public void batchDelete(List<Long> ids) {
        questionRepository.deleteAllById(ids);
    }

    // ===== Onboarding Questions =====
    public List<OnboardingQuestion> getAllOnboardingQuestions() {
        return onboardingRepository.findAll();
    }

    public Optional<OnboardingQuestion> getOnboardingById(Integer id) {
        return onboardingRepository.findById(id);
    }

    @Transactional
    public OnboardingQuestion addOnboardingQuestion(OnboardingQuestion q) {
        return onboardingRepository.save(q);
    }

    @Transactional
    public OnboardingQuestion updateOnboardingQuestion(Integer id, OnboardingQuestion updated) {
        OnboardingQuestion q = onboardingRepository.findById(id).orElseThrow();
        q.setGroupId(updated.getGroupId());
        q.setTypeId(updated.getTypeId());
        q.setImageUrl(updated.getImageUrl());
        q.setQuestion(updated.getQuestion());
        q.setIsMulti(updated.getIsMulti());
        q.setOptions(updated.getOptions());
        q.setAnswer(updated.getAnswer());
        q.setAnalysis(updated.getAnalysis());
        return onboardingRepository.save(q);
    }

    @Transactional
    public void deleteOnboardingQuestion(Integer id) {
        onboardingRepository.deleteById(id);
    }

    // ===== DTO Conversion =====
    /**
     * 转换为安全发卷模型，只返回作答所需字段。
     */
    public static ExamPaperQuestionDTO toExamPaperDTO(Question q) {
        ExamPaperQuestionDTO dto = new ExamPaperQuestionDTO();
        dto.setId(q.getId());
        dto.setType(q.getType());
        dto.setDifficulty(q.getDifficulty());
        dto.setCategory(q.getCategory());
        dto.setResource(q.getResource());
        dto.setQuestion(q.getQuestion());
        dto.setPicture(q.getHasPicture());
        dto.setPictureUrl(q.getHasPicture() != null && q.getHasPicture()
                ? "/images/" + q.getId() + ".png" : q.getPictureUrl());
        dto.setOptions(q.getOptions() != null ? Arrays.asList(q.getOptions().split("\\|")) : List.of());
        return dto;
    }

    public static QuestionDTO toDTO(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(q.getId());
        dto.setType(q.getType());
        dto.setDifficulty(q.getDifficulty());
        dto.setCategory(q.getCategory());
        dto.setResource(q.getResource());
        dto.setQuestion(q.getQuestion());
        dto.setPicture(q.getHasPicture());
        dto.setPictureUrl(q.getHasPicture() != null && q.getHasPicture()
                ? "/images/" + q.getId() + ".png" : q.getPictureUrl());
        dto.setOptions(q.getOptions() != null ? Arrays.asList(q.getOptions().split("\\|")) : List.of());
        dto.setAnswer(q.getAnswer() != null ? Integer.parseInt(q.getAnswer()) : 0);
        dto.setAnalysis(q.getAnalysis());
        dto.setKeywords(q.getKeywords() != null ? Arrays.asList(q.getKeywords().split("\\|")) : List.of());
        dto.setViewCount(q.getViewCount());
        dto.setErrorCount(q.getErrorCount());
        dto.setCreatedAt(q.getCreatedAt());
        dto.setUpdatedAt(q.getUpdatedAt());
        return dto;
    }

    public static QuestionDTO toDTOFromOnboarding(OnboardingQuestion o) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(o.getId() != null ? o.getId().longValue() : null);
        dto.setType(o.getTypeId());
        dto.setDifficulty(1);
        dto.setQuestion(o.getQuestion());
        boolean hasPic = o.getImageUrl() != null && !o.getImageUrl().isEmpty();
        dto.setPicture(hasPic);
        dto.setPictureUrl(hasPic ? "/images/" + o.getId() + ".png" : o.getImageUrl());
        dto.setOptions(o.getOptions() != null ? Arrays.asList(o.getOptions().split("\\|")) : List.of());
        dto.setAnswer(o.getAnswer() != null ? Integer.parseInt(o.getAnswer()) : 0);
        dto.setAnalysis(o.getAnalysis());
        dto.setKeywords(List.of());
        dto.setViewCount(0);
        dto.setErrorCount(0);
        return dto;
    }
}
