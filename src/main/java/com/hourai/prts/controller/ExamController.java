package com.hourai.prts.controller;

import com.hourai.prts.common.Result;
import com.hourai.prts.common.ResultCode;
import com.hourai.prts.dto.QuestionDTO;
import com.hourai.prts.entity.*;
import com.hourai.prts.repository.*;
import com.hourai.prts.service.ExamService;
import com.hourai.prts.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class ExamController {

    private final ExamService examService;
    private final QuestionService questionService;
    private final UserAnswerRepository userAnswerRepository;
    private final WrongQuestionVisibilityRepository wrongVisibilityRepository;
    private final QuestionRepository questionRepository;

    public ExamController(ExamService examService, QuestionService questionService,
                          UserAnswerRepository userAnswerRepository,
                          WrongQuestionVisibilityRepository wrongVisibilityRepository,
                          QuestionRepository questionRepository) {
        this.examService = examService;
        this.questionService = questionService;
        this.userAnswerRepository = userAnswerRepository;
        this.wrongVisibilityRepository = wrongVisibilityRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping("/exam/paper")
    public ResponseEntity<Result<List<QuestionDTO>>> generatePaper() {
        List<Question> paper = examService.generatePaper();
        List<QuestionDTO> dtos = paper.stream().map(QuestionService::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(Result.success(dtos));
    }

    @PostMapping("/exam/submit")
    public ResponseEntity<Result<Map<String, Object>>> submitExam(@RequestParam Long userId,
                                                                  @RequestParam String answers,
                                                                  @RequestParam(required = false) Integer duration) {
        if (userId == null || answers == null || answers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.fail(ResultCode.BAD_REQUEST, "缺少必填字段"));
        }
        Map<Long, Integer> answerMap = parseAnswers(answers);
        ExamRecord record = examService.submitExam(userId, answerMap, duration);
        Map<String, Object> data = Map.of("examId", record.getId(), "score", record.getScore().intValue());
        return ResponseEntity.ok(Result.success(data));
    }

    @GetMapping("/exam/history")
    public ResponseEntity<Result<List<Map<String, Object>>>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        List<ExamRecord> records;
        if (userId != null) {
            records = examService.getHistoryByUser(userId, page, size);
        } else {
            records = examService.getHistory(page, size);
        }
        List<Map<String, Object>> result = records.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("examId", r.getId());
            m.put("userId", r.getUserId());
            m.put("score", r.getScore().intValue());
            m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Result.success(result));
    }

    // ===== 错题 =====

    @GetMapping("/answers/wrong")
    public ResponseEntity<Result<List<Map<String, Object>>>> getWrongQuestions(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();

        List<UserAnswer> wrongAnswers = userAnswerRepository.findByUserIdAndIsCorrectFalse(userId);
        List<WrongQuestionVisibility> hidden = wrongVisibilityRepository.findByUserIdAndHiddenTrue(userId);
        Set<Long> hiddenIds = hidden.stream().map(WrongQuestionVisibility::getQuestionId).collect(Collectors.toSet());

        Set<Long> seenIds = new HashSet<>();
        List<Question> wrongQuestions = new ArrayList<>();
        for (UserAnswer ua : wrongAnswers) {
            if (hiddenIds.contains(ua.getQuestionId())) continue;
            if (!seenIds.add(ua.getQuestionId())) continue;
            questionRepository.findById(ua.getQuestionId()).ifPresent(wrongQuestions::add);
        }

        List<Map<String, Object>> result = wrongQuestions.stream().map(q -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", q.getId());
            m.put("question", q.getQuestion());
            m.put("type", q.getType());
            m.put("difficulty", q.getDifficulty());
            m.put("options", q.getOptions() != null ? Arrays.asList(q.getOptions().split("\\|")) : List.of());
            m.put("answer", q.getAnswer());
            m.put("analysis", q.getAnalysis());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Result.success(result));
    }

    @DeleteMapping("/answers/wrong/{questionId}")
    @Transactional
    public ResponseEntity<Result<Map<String, Object>>> hideWrongQuestion(@PathVariable Long questionId, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();

        WrongQuestionVisibility wv = wrongVisibilityRepository
                .findByUserIdAndQuestionId(userId, questionId)
                .orElseGet(() -> {
                    WrongQuestionVisibility n = new WrongQuestionVisibility();
                    n.setUserId(userId);
                    n.setQuestionId(questionId);
                    return n;
                });
        wv.setHidden(true);
        wv.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        wrongVisibilityRepository.save(wv);

        return ResponseEntity.ok(Result.success(Map.of("questionId", questionId)));
    }

    @GetMapping("/user/{id}/wrong")
    public ResponseEntity<Result<List<QuestionDTO>>> getUserWrongQuestions(@PathVariable Long id) {
        List<UserAnswer> wrongAnswers = userAnswerRepository.findByUserIdAndIsCorrectFalse(id);
        Set<Long> seenIds = new HashSet<>();
        List<Question> wrongQuestions = new ArrayList<>();
        for (UserAnswer ua : wrongAnswers) {
            if (!seenIds.add(ua.getQuestionId())) continue;
            questionRepository.findById(ua.getQuestionId()).ifPresent(wrongQuestions::add);
        }
        List<QuestionDTO> dtos = wrongQuestions.stream().map(QuestionService::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(Result.success(dtos));
    }

    private Map<Long, Integer> parseAnswers(String s) {
        Map<Long, Integer> m = new LinkedHashMap<>();
        if (s == null || s.trim().isEmpty()) return m;
        for (String part : s.split(",")) {
            String[] kv = part.split(":");
            if (kv.length != 2) continue;
            try {
                m.put(Long.parseLong(kv[0].trim()), Integer.parseInt(kv[1].trim()));
            } catch (Exception ignored) {}
        }
        return m;
    }
}
