package com.hourai.prts.service;

import com.hourai.prts.entity.ExamDetail;
import com.hourai.prts.entity.ExamRecord;
import com.hourai.prts.entity.Question;
import com.hourai.prts.entity.UserAnswer;
import com.hourai.prts.repository.ExamDetailRepository;
import com.hourai.prts.repository.ExamRecordRepository;
import com.hourai.prts.repository.QuestionRepository;
import com.hourai.prts.repository.UserAnswerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExamService {
    private final QuestionRepository questionRepository;
    private final ExamRecordRepository examRecordRepository;
    private final ExamDetailRepository examDetailRepository;
    private final UserAnswerRepository userAnswerRepository;

    public ExamService(QuestionRepository questionRepository, ExamRecordRepository examRecordRepository,
                       ExamDetailRepository examDetailRepository, UserAnswerRepository userAnswerRepository) {
        this.questionRepository = questionRepository;
        this.examRecordRepository = examRecordRepository;
        this.examDetailRepository = examDetailRepository;
        this.userAnswerRepository = userAnswerRepository;
    }

    public List<Question> generatePaper() {
        List<Question> paper = new ArrayList<>();
        Random rng = new Random();
        for (int type = 1; type <= 5; type++) {
            for (int diff = 1; diff <= 5; diff++) {
                List<Question> candidates = questionRepository.findByTypeAndDifficulty(type, diff, PageRequest.of(0, 100)).getContent();
                if (!candidates.isEmpty()) {
                    paper.add(candidates.get(rng.nextInt(candidates.size())));
                }
            }
        }
        return paper;
    }

    @Transactional
    public ExamRecord submitExam(Long userId, Map<Long, Integer> answers, Integer duration) {
        int correctCount = 0;
        int total = answers.size();

        for (Map.Entry<Long, Integer> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            int selectedAnswer = entry.getValue();

            Optional<Question> qOpt = questionRepository.findById(questionId);
            if (qOpt.isPresent()) {
                Question q = qOpt.get();
                int correctAnswer = Integer.parseInt(q.getAnswer());
                boolean isCorrect = (selectedAnswer == correctAnswer);
                if (isCorrect) correctCount++;

                ExamDetail detail = new ExamDetail();
                detail.setExamId(null);
                detail.setQuestionId(questionId);
                detail.setSelectedAnswer(String.valueOf(selectedAnswer));
                detail.setIsCorrect(isCorrect);

                UserAnswer ua = new UserAnswer();
                ua.setUserId(userId);
                ua.setQuestionId(questionId);
                ua.setSelectedAnswer(String.valueOf(selectedAnswer));
                ua.setIsCorrect(isCorrect);
                ua.setCreatedAt(LocalDateTime.now());
                userAnswerRepository.save(ua);
            }
        }

        BigDecimal score = total > 0
                ? BigDecimal.valueOf(correctCount * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setTotalQuestions(total);
        record.setCorrectCount(correctCount);
        record.setScore(score);
        record.setDuration(duration);
        record.setCreatedAt(LocalDateTime.now());
        return examRecordRepository.save(record);
    }

    public List<ExamRecord> getHistory(int page, int size) {
        return examRecordRepository.findAll(PageRequest.of(page - 1, size)).getContent();
    }

    public List<ExamRecord> getHistoryByUser(Long userId, int page, int size) {
        return examRecordRepository.findByUserId(userId, PageRequest.of(page - 1, size)).getContent();
    }

    public Map<String, Object> getQuestionStats(Long questionId) {
        long totalUsers = userAnswerRepository.countByQuestionId(questionId);
        long correctUsers = userAnswerRepository.countByQuestionIdAndIsCorrectTrue(questionId);
        double correctRate = totalUsers > 0 ? (double) correctUsers / totalUsers : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("correctRate", Math.round(correctRate * 10000.0) / 10000.0);
        stats.put("mostCommonWrongOption", 0);
        return stats;
    }
}
