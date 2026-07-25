package com.hourai.prts.service;

import com.hourai.prts.entity.ExamDetail;
import com.hourai.prts.entity.ExamRecord;
import com.hourai.prts.entity.Question;
import com.hourai.prts.entity.UserAnswer;
import com.hourai.prts.dto.ExamQuestionResultDTO;
import com.hourai.prts.dto.ExamSubmissionResultDTO;
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

/**
 * 正式考试的组卷、判分、记录和统计服务。
 *
 * <p>交卷事务同时写入考试汇总、用户答题行为和考试明细，任一步骤失败都会回滚，
 * 防止出现分数已保存但答题明细缺失的不一致状态。
 */
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

    /**
     * 按 5 种题型和 5 个难度组合各随机抽取一道题。
     * 某个组合没有候选题时跳过，因此实际题量可能少于 25。
     */
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

    /**
     * 使用数据库中的正确答案判分，客户端只负责提交选择结果。
     *
     * @param userId   从已验证 JWT principal 中取得的当前用户 ID
     * @param answers  题目 ID 到用户选项的映射
     * @param duration 前端统计的答题用时（秒）
     */
    @Transactional
    public ExamSubmissionResultDTO submitExam(Long userId, Map<Long, Integer> answers, Integer duration) {
        int correctCount = 0;
        int total = 0;
        List<ExamQuestionResultDTO> questionResults = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            int selectedAnswer = entry.getValue();

            Optional<Question> qOpt = questionRepository.findById(questionId);
            if (qOpt.isPresent()) {
                Question q = qOpt.get();
                total++;
                int correctAnswer = Integer.parseInt(q.getAnswer());
                boolean isCorrect = (selectedAnswer == correctAnswer);
                if (isCorrect) correctCount++;

                UserAnswer ua = new UserAnswer();
                ua.setUserId(userId);
                ua.setQuestionId(questionId);
                ua.setSelectedAnswer(String.valueOf(selectedAnswer));
                ua.setIsCorrect(isCorrect);
                ua.setCreatedAt(LocalDateTime.now());
                userAnswerRepository.save(ua);

                questionResults.add(new ExamQuestionResultDTO(
                        questionId, selectedAnswer, correctAnswer, isCorrect, q.getAnalysis()));
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
        ExamRecord savedRecord = examRecordRepository.save(record);

        List<ExamDetail> details = questionResults.stream().map(result -> {
            ExamDetail detail = new ExamDetail();
            detail.setExamId(savedRecord.getId());
            detail.setQuestionId(result.getId());
            detail.setSelectedAnswer(String.valueOf(result.getSelectedAnswer()));
            detail.setIsCorrect(result.getCorrect());
            return detail;
        }).toList();
        examDetailRepository.saveAll(details);

        ExamSubmissionResultDTO result = new ExamSubmissionResultDTO();
        result.setExamId(savedRecord.getId());
        result.setScore(savedRecord.getScore());
        result.setTotalQuestions(total);
        result.setCorrectCount(correctCount);
        result.setQuestions(questionResults);
        return result;
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
