package com.hourai.prts.service;

import com.hourai.prts.common.BusinessException;
import com.hourai.prts.common.ResultCode;
import com.hourai.prts.dto.ExamPaperDTO;
import com.hourai.prts.dto.ExamQuestionResultDTO;
import com.hourai.prts.dto.ExamSubmissionResultDTO;
import com.hourai.prts.entity.*;
import com.hourai.prts.repository.*;
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
    private final ExamPaperRepository examPaperRepository;
    private final ExamPaperQuestionRepository examPaperQuestionRepository;

    public ExamService(QuestionRepository questionRepository, ExamRecordRepository examRecordRepository,
                       ExamDetailRepository examDetailRepository, UserAnswerRepository userAnswerRepository,
                       ExamPaperRepository examPaperRepository,
                       ExamPaperQuestionRepository examPaperQuestionRepository) {
        this.questionRepository = questionRepository;
        this.examRecordRepository = examRecordRepository;
        this.examDetailRepository = examDetailRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.examPaperRepository = examPaperRepository;
        this.examPaperQuestionRepository = examPaperQuestionRepository;
    }

    /**
     * 按 5 种题型和 5 个难度组合各随机抽取一道题。
     * 某个组合没有候选题时跳过，因此实际题量可能少于 25。
     */
    @Transactional
    public ExamPaperDTO generatePaper(Long userId) {
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
        if (paper.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "题库中没有可用于组卷的题目");
        }

        ExamPaper examPaper = new ExamPaper();
        examPaper.setUserId(userId);
        examPaper.setStatus(ExamPaper.STATUS_ACTIVE);
        examPaper.setCreatedAt(LocalDateTime.now());
        ExamPaper savedPaper = examPaperRepository.save(examPaper);

        List<ExamPaperQuestion> paperQuestions = new ArrayList<>();
        for (int i = 0; i < paper.size(); i++) {
            ExamPaperQuestion paperQuestion = new ExamPaperQuestion();
            paperQuestion.setPaperId(savedPaper.getId());
            paperQuestion.setQuestionId(paper.get(i).getId());
            paperQuestion.setPosition(i + 1);
            paperQuestions.add(paperQuestion);
        }
        examPaperQuestionRepository.saveAll(paperQuestions);

        return new ExamPaperDTO(
                savedPaper.getId(),
                paper.stream().map(QuestionService::toExamPaperDTO).toList());
    }

    /**
     * 使用数据库中的正确答案判分，客户端只负责提交选择结果。
     *
     * @param userId   从已验证 JWT principal 中取得的当前用户 ID
     * @param paperId  服务端生成并绑定到当前用户的试卷 ID
     * @param answers  题目 ID 到用户选项的映射；缺少的题目视为未作答
     * @param duration 前端统计的答题用时（秒）
     */
    @Transactional
    public ExamSubmissionResultDTO submitExam(Long userId, Long paperId,
                                              Map<Long, Integer> answers, Integer duration) {
        ExamPaper paper = examPaperRepository.findByIdAndUserId(paperId, userId)
                .orElseThrow(() -> new BusinessException(
                        ResultCode.NOT_FOUND, "试卷不存在或不属于当前用户"));
        if (!ExamPaper.STATUS_ACTIVE.equals(paper.getStatus())) {
            throw new BusinessException(ResultCode.CONFLICT, "该试卷已经提交，请勿重复交卷");
        }

        List<ExamPaperQuestion> issuedQuestions =
                examPaperQuestionRepository.findByPaperIdOrderByPositionAsc(paperId);
        if (issuedQuestions.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "试卷中没有题目");
        }

        List<Long> issuedIds = issuedQuestions.stream()
                .map(ExamPaperQuestion::getQuestionId)
                .toList();
        Set<Long> issuedIdSet = new HashSet<>(issuedIds);
        if (!issuedIdSet.containsAll(answers.keySet())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "答案中包含不属于该试卷的题目");
        }

        Map<Long, Question> questionsById = new HashMap<>();
        questionRepository.findAllById(issuedIds)
                .forEach(question -> questionsById.put(question.getId(), question));
        if (questionsById.size() != issuedIds.size()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "试卷题目已发生变更，请重新生成试卷");
        }

        int correctCount = 0;
        int total = issuedIds.size();
        List<ExamQuestionResultDTO> questionResults = new ArrayList<>();
        LocalDateTime submittedAt = LocalDateTime.now();

        for (Long questionId : issuedIds) {
            Question question = questionsById.get(questionId);
            Integer selectedAnswer = answers.get(questionId);
            int correctAnswer = Integer.parseInt(question.getAnswer());
            boolean isCorrect = selectedAnswer != null && selectedAnswer == correctAnswer;
            if (isCorrect) correctCount++;

            // 未作答会进入考试明细并参与分母，但不写入错题本，避免空题污染错题统计。
            if (selectedAnswer != null) {
                UserAnswer userAnswer = new UserAnswer();
                userAnswer.setUserId(userId);
                userAnswer.setQuestionId(questionId);
                userAnswer.setSelectedAnswer(String.valueOf(selectedAnswer));
                userAnswer.setIsCorrect(isCorrect);
                userAnswer.setCreatedAt(submittedAt);
                userAnswerRepository.save(userAnswer);
            }

            questionResults.add(new ExamQuestionResultDTO(
                    questionId, selectedAnswer, correctAnswer, isCorrect, question.getAnalysis()));
        }

        BigDecimal score = BigDecimal.valueOf(correctCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setTotalQuestions(total);
        record.setCorrectCount(correctCount);
        record.setScore(score);
        record.setDuration(duration);
        record.setCreatedAt(submittedAt);
        ExamRecord savedRecord = examRecordRepository.save(record);

        List<ExamDetail> details = questionResults.stream().map(result -> {
            ExamDetail detail = new ExamDetail();
            detail.setExamId(savedRecord.getId());
            detail.setQuestionId(result.getId());
            detail.setSelectedAnswer(result.getSelectedAnswer() == null
                    ? null : String.valueOf(result.getSelectedAnswer()));
            detail.setIsCorrect(result.getCorrect());
            return detail;
        }).toList();
        examDetailRepository.saveAll(details);

        paper.setStatus(ExamPaper.STATUS_SUBMITTED);
        paper.setSubmittedAt(submittedAt);
        examPaperRepository.save(paper);

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
