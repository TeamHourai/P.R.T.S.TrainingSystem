package com.hourai.prts.controller;

import com.hourai.prts.common.Result;
import com.hourai.prts.common.ResultCode;
import com.hourai.prts.service.ExamService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Result<Map<String, Object>>> ping() {
        return ResponseEntity.ok(Result.success(Map.of("ok", true)));
    }

    @GetMapping("/api/v1/stats/question/{id}")
    public ResponseEntity<Result<Map<String, Object>>> questionStats(@PathVariable Long id) {
        try {
            Map<String, Object> stats = examService.getQuestionStats(id);
            return ResponseEntity.ok(Result.success(stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.fail(ResultCode.INTERNAL_ERROR, "统计失败"));
        }
    }

    @GetMapping("/api/v1/stats/user")
    public ResponseEntity<Result<Map<String, Object>>> userStats() {
        return ResponseEntity.ok(Result.success(Map.of("totalAttempts", 0, "correctRate", 0, "totalUsers", 0)));
    }

    @GetMapping("/api/v1/stats/system")
    public ResponseEntity<Result<Map<String, Object>>> systemStats() {
        return ResponseEntity.ok(Result.success(Map.of("status", "ok")));
    }
}
