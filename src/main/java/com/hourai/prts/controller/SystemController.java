package com.hourai.prts.controller;

import com.hourai.prts.service.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class SystemController {

    private final ExamService examService;

    public SystemController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping({"/ping", "/api/v1/ping"})
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/api/v1/stats/question/{id}")
    public ResponseEntity<?> questionStats(@PathVariable Long id) {
        try {
            Map<String, Object> stats = examService.getQuestionStats(id);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "internal error"));
        }
    }

    @GetMapping("/api/v1/stats/user")
    public ResponseEntity<?> userStats() {
        return ResponseEntity.ok(Map.of("totalAttempts", 0, "correctRate", 0, "totalUsers", 0));
    }

    @GetMapping("/api/v1/stats/system")
    public ResponseEntity<?> systemStats() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
