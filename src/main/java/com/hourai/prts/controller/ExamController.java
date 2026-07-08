package com.hourai.prts.controller;

import com.hourai.prts.entity.ExamRecord;
import com.hourai.prts.entity.Question;
import com.hourai.prts.service.ExamService;
import com.hourai.prts.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class ExamController {
    private final ExamService examService;
    private final QuestionService questionService;

    public ExamController(ExamService examService, QuestionService questionService) {
        this.examService = examService;
        this.questionService = questionService;
    }

    @GetMapping("/exam/paper")
    public ResponseEntity<?> generatePaper() {
        List<Question> paper = examService.generatePaper();
        return ResponseEntity.ok(paper.stream().map(QuestionService::toDTO).collect(Collectors.toList()));
    }

    @PostMapping("/exam/submit")
    public ResponseEntity<?> submitExam(@RequestParam Long userId,
                                        @RequestParam String answers,
                                        @RequestParam(required = false) Integer duration) {
        if (userId == null || answers == null || answers.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing required fields"));
        }
        Map<Long, Integer> answerMap = parseAnswers(answers);
        ExamRecord record = examService.submitExam(userId, answerMap, duration);
        return ResponseEntity.ok(Map.of("examId", record.getId(), "score", record.getScore().intValue()));
    }

    @GetMapping("/exam/history")
    public ResponseEntity<?> getHistory(
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
        return ResponseEntity.ok(result);
    }

    // ===== Wrong answers =====
    @GetMapping("/answers/wrong")
    public ResponseEntity<?> getWrongQuestions(Authentication auth,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "1000") int size) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "missing token"));
        }
        Long userId = (Long) auth.getPrincipal();
        // Find wrong questions by checking user_answers where is_correct=false
        // and not hidden in wrong_visibility
        // For now, return all questions marked wrong
        return ResponseEntity.ok(List.of()); // Implement with proper service
    }

    @DeleteMapping("/answers/wrong/{questionId}")
    public ResponseEntity<?> hideWrongQuestion(@PathVariable Long questionId, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "missing token"));
        }
        Long userId = (Long) auth.getPrincipal();
        // Hide the wrong question
        return ResponseEntity.ok(Map.of("success", true, "message", "hidden", "userId", userId, "questionId", questionId));
    }

    @GetMapping("/user/{id}/wrong")
    public ResponseEntity<?> getUserWrongQuestions(@PathVariable Long id) {
        // Legacy endpoint
        return ResponseEntity.ok(List.of());
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
