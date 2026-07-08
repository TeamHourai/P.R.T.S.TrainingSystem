package com.hourai.prts.controller;

import com.hourai.prts.entity.AnswerSettings;
import com.hourai.prts.entity.TrainingRecord;
import com.hourai.prts.repository.AnswerSettingsRepository;
import com.hourai.prts.repository.TrainingRecordRepository;
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

    // ===== Answer Settings =====
    @GetMapping("/answer-settings")
    public ResponseEntity<?> getAnswerSettings(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "missing token"));
        Long userId = (Long) auth.getPrincipal();
        Optional<AnswerSettings> settings = answerSettingsRepository.findById(userId);
        AnswerSettings s = settings.orElseGet(() -> {
            AnswerSettings def = new AnswerSettings();
            def.setUserId(userId);
            def.setAutoSubmit(false);
            def.setAutoNextCorrect(true);
            return def;
        });
        return ResponseEntity.ok(Map.of(
            "success", true,
            "autoSubmit", s.getAutoSubmit(),
            "autoNextCorrect", s.getAutoNextCorrect()
        ));
    }

    @PutMapping("/answer-settings")
    @Transactional
    public ResponseEntity<?> updateAnswerSettings(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "missing token"));
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
        return ResponseEntity.ok(Map.of(
            "success", true,
            "autoSubmit", s.getAutoSubmit(),
            "autoNextCorrect", s.getAutoNextCorrect()
        ));
    }

    // ===== Training Records =====
    @GetMapping("/training-records")
    public ResponseEntity<?> getTrainingRecords(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "missing token"));
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
        return ResponseEntity.ok(Map.of("success", true, "records", result));
    }

    @PostMapping("/training-records")
    @PutMapping("/training-records")
    @Transactional
    public ResponseEntity<?> saveTrainingRecord(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "missing token"));
        Long userId = (Long) auth.getPrincipal();

        Object qIdObj = body.get("questionId");
        if (qIdObj == null) return ResponseEntity.badRequest().body(Map.of("error", "questionId required"));
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
        return ResponseEntity.ok(Map.of(
            "success", true,
            "questionId", tr.getQuestionId(),
            "attempts", tr.getAttempts(),
            "correct", tr.getCorrect(),
            "lastAt", tr.getLastAt()
        ));
    }

    @DeleteMapping("/training-records")
    @Transactional
    public ResponseEntity<?> deleteTrainingRecords(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "missing token"));
        Long userId = (Long) auth.getPrincipal();
        trainingRecordRepository.deleteByUserId(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
