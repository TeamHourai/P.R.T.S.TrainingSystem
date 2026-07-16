package com.hourai.prts.controller;

import com.hourai.prts.common.Result;
import com.hourai.prts.common.ResultCode;
import com.hourai.prts.entity.AnswerSettings;
import com.hourai.prts.entity.TrainingRecord;
import com.hourai.prts.repository.AnswerSettingsRepository;
import com.hourai.prts.repository.TrainingRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserSettingsController {

    private final AnswerSettingsRepository answerSettingsRepository;
    private final TrainingRecordRepository trainingRecordRepository;

    public UserSettingsController(AnswerSettingsRepository answerSettingsRepository,
                                   TrainingRecordRepository trainingRecordRepository) {
        this.answerSettingsRepository = answerSettingsRepository;
        this.trainingRecordRepository = trainingRecordRepository;
    }

    // ===== 答题设置 =====
    @GetMapping("/answer-settings")
    public ResponseEntity<Result<Map<String, Object>>> getAnswerSettings(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();
        AnswerSettings s = answerSettingsRepository.findById(userId).orElseGet(() -> {
            AnswerSettings def = new AnswerSettings();
            def.setUserId(userId);
            def.setAutoSubmit(false);
            def.setAutoNextCorrect(true);
            return def;
        });
        Map<String, Object> data = Map.of("autoSubmit", s.getAutoSubmit(), "autoNextCorrect", s.getAutoNextCorrect());
        return ResponseEntity.ok(Result.success(data));
    }

    @PutMapping("/answer-settings")
    @Transactional
    public ResponseEntity<Result<Map<String, Object>>> updateAnswerSettings(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();
        AnswerSettings s = answerSettingsRepository.findById(userId).orElseGet(() -> {
            AnswerSettings def = new AnswerSettings();
            def.setUserId(userId);
            return def;
        });
        if (body.containsKey("autoSubmit")) s.setAutoSubmit((Boolean) body.get("autoSubmit"));
        if (body.containsKey("autoNextCorrect")) s.setAutoNextCorrect((Boolean) body.get("autoNextCorrect"));
        s.setUpdatedAt(LocalDateTime.now());
        answerSettingsRepository.save(s);
        Map<String, Object> data = Map.of("autoSubmit", s.getAutoSubmit(), "autoNextCorrect", s.getAutoNextCorrect());
        return ResponseEntity.ok(Result.success(data));
    }

    // ===== 培训记录 =====
    @GetMapping("/training-records")
    public ResponseEntity<Result<Map<String, Object>>> getTrainingRecords(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();
        List<TrainingRecord> records = trainingRecordRepository.findByUserId(userId);
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (TrainingRecord r : records) {
            Map<String, Object> m = new HashMap<>();
            m.put("attempts", r.getAttempts());
            m.put("correct", r.getCorrect());
            m.put("lastAt", r.getLastAt());
            result.put(String.valueOf(r.getQuestionId()), m);
        }
        return ResponseEntity.ok(Result.success(Map.of("records", result)));
    }

    @RequestMapping(value = "/training-records", method = {RequestMethod.POST, RequestMethod.PUT})
    @Transactional
    public ResponseEntity<Result<Map<String, Object>>> saveTrainingRecord(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();

        Object qIdObj = body.get("questionId");
        if (qIdObj == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.fail(ResultCode.BAD_REQUEST, "缺少 questionId"));
        }
        Long questionId = Long.parseLong(String.valueOf(qIdObj));

        TrainingRecord tr = trainingRecordRepository.findById(new TrainingRecord.TrainingRecordId(userId, questionId))
                .orElseGet(() -> {
                    TrainingRecord n = new TrainingRecord();
                    n.setUserId(userId);
                    n.setQuestionId(questionId);
                    return n;
                });

        if (body.containsKey("attempts")) tr.setAttempts(Math.max(0, (Integer) body.get("attempts")));
        if (body.containsKey("correct")) tr.setCorrect((Boolean) body.get("correct"));
        if (body.containsKey("lastAt")) {
            long lastAt = Long.parseLong(String.valueOf(body.get("lastAt")));
            tr.setLastAt(lastAt > 0 ? lastAt : System.currentTimeMillis());
        }
        trainingRecordRepository.save(tr);
        Map<String, Object> data = Map.of(
                "questionId", tr.getQuestionId(),
                "attempts", tr.getAttempts(),
                "correct", tr.getCorrect(),
                "lastAt", tr.getLastAt()
        );
        return ResponseEntity.ok(Result.success(data));
    }

    @DeleteMapping("/training-records")
    @Transactional
    public ResponseEntity<Result<Void>> deleteTrainingRecords(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();
        trainingRecordRepository.deleteByUserId(userId);
        return ResponseEntity.ok(Result.success("已清除培训记录", null));
    }
}
