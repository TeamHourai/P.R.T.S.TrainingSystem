package com.hourai.prts.controller;

import com.hourai.prts.dto.ApiResponse;
import com.hourai.prts.dto.QuestionDTO;
import com.hourai.prts.entity.OnboardingQuestion;
import com.hourai.prts.entity.Question;
import com.hourai.prts.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // ===== 正式题库 =====
    @GetMapping("/questions")
    public ResponseEntity<?> listQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mode) {

        // Support mode=onboarding (called by trainingQuestionApi.js)
        if ("onboarding".equals(mode)) {
            List<OnboardingQuestion> all = questionService.getAllOnboardingQuestions();
            int total = all.size();
            int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 1;
            int from = Math.min((page - 1) * size, total);
            int to = Math.min(from + size, total);
            List<OnboardingQuestion> pageList = all.subList(from, to);
            List<QuestionDTO> dtos = pageList.stream().map(QuestionService::toDTOFromOnboarding).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of(
                "questions", dtos, "total", total, "page", page, "size", size, "pages", totalPages
            ));
        }

        List<Question> filtered = questionService.getFilteredQuestions(type, difficulty, keyword);
        int total = filtered.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 1;
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<Question> pageList = filtered.subList(from, to);
        List<QuestionDTO> dtos = pageList.stream().map(QuestionService::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
            "questions", dtos, "total", total, "page", page, "size", size, "pages", totalPages
        ));
    }

    @GetMapping("/questions/{id}")
    public ResponseEntity<?> getQuestion(@PathVariable Long id) {
        Optional<Question> q = questionService.getQuestionById(id);
        return q.map(value -> ResponseEntity.ok(QuestionService.toDTO(value)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/questions")
    public ResponseEntity<?> createQuestion(@RequestBody Map<String, Object> body) {
        Question q = mapToQuestion(body);
        q = questionService.addQuestion(q);
        return ResponseEntity.ok(Map.of("id", q.getId()));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Question updated = mapToQuestion(body);
            questionService.updateQuestion(id, updated);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ===== 入职培训题库 =====
    @GetMapping("/training/questions")
    public ResponseEntity<?> listTrainingQuestions() {
        List<OnboardingQuestion> all = questionService.getAllOnboardingQuestions();
        List<QuestionDTO> dtos = all.stream().map(QuestionService::toDTOFromOnboarding).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/training/questions/{id}")
    public ResponseEntity<?> getTrainingQuestion(@PathVariable Integer id) {
        Optional<OnboardingQuestion> q = questionService.getOnboardingById(id);
        return q.map(value -> ResponseEntity.ok(QuestionService.toDTOFromOnboarding(value)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/training/questions")
    public ResponseEntity<?> createTrainingQuestion(@RequestBody Map<String, Object> body) {
        OnboardingQuestion q = mapToOnboarding(body);
        q = questionService.addOnboardingQuestion(q);
        return ResponseEntity.ok(Map.of("id", q.getId()));
    }

    @PutMapping("/training/questions/{id}")
    public ResponseEntity<?> updateTrainingQuestion(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        OnboardingQuestion q = mapToOnboarding(body);
        q.setId(id);
        questionService.updateOnboardingQuestion(id, q);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/training/questions/{id}")
    public ResponseEntity<?> deleteTrainingQuestion(@PathVariable Integer id) {
        questionService.deleteOnboardingQuestion(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ===== Keywords =====
    @GetMapping("/keywords")
    public ResponseEntity<?> getKeywords(@RequestParam(defaultValue = "") String mode) {
        Set<String> keywords = new LinkedHashSet<>();
        if ("onboarding".equals(mode)) {
            questionService.getAllOnboardingQuestions().forEach(o -> {
                // onboarding doesn't have keywords field; skip
            });
        } else {
            questionService.getAllQuestions().forEach(q -> {
                if (q.getKeywords() != null && !q.getKeywords().isEmpty()) {
                    for (String kw : q.getKeywords().split("\\|")) {
                        if (!kw.trim().isEmpty()) keywords.add(kw.trim());
                    }
                }
            });
        }
        return ResponseEntity.ok(new ArrayList<>(keywords));
    }

    // ===== Admin batch delete =====
    @PostMapping("/admin/questions/batch-delete")
    public ResponseEntity<?> batchDelete(@RequestBody Map<String, Object> body) {
        Object idsObj = body.get("ids");
        List<Long> ids = new ArrayList<>();
        if (idsObj instanceof List) {
            for (Object item : (List<?>) idsObj) {
                ids.add(Long.parseLong(String.valueOf(item)));
            }
        } else if (idsObj instanceof String) {
            for (String s : ((String) idsObj).split(",")) {
                ids.add(Long.parseLong(s.trim()));
            }
        }
        if (ids.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "no ids provided"));
        }
        questionService.batchDelete(ids);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ===== Helper methods =====
    @SuppressWarnings("unchecked")
    private Question mapToQuestion(Map<String, Object> body) {
        Question q = new Question();
        q.setType(toInt(body.get("type"), 1));
        q.setDifficulty(toInt(body.get("difficulty"), 1));
        q.setQuestion(toStr(body.get("question")));
        q.setAnalysis(toStr(body.get("analysis")));
        q.setResource(toStr(body.get("resource")));
        q.setHasPicture(toBool(body.get("picture")));
        q.setPictureUrl(toStr(body.get("pictureUrl")));

        Object opts = body.get("options");
        if (opts instanceof List) {
            q.setOptions(String.join("|", ((List<Object>) opts).stream().map(Object::toString).toList()));
        } else if (opts != null) {
            q.setOptions(opts.toString());
        }
        q.setAnswer(toStr(body.get("answer")));

        Object kw = body.get("keywords");
        if (kw instanceof List) {
            q.setKeywords(String.join("|", ((List<Object>) kw).stream().map(Object::toString).toList()));
        } else if (kw != null) {
            q.setKeywords(kw.toString().replace(",", "|"));
        }
        return q;
    }

    @SuppressWarnings("unchecked")
    private OnboardingQuestion mapToOnboarding(Map<String, Object> body) {
        OnboardingQuestion o = new OnboardingQuestion();
        o.setGroupId(toInt(body.get("group_id"), body.get("groupId"), body.get("group")));
        o.setTypeId(toInt(body.get("type_id"), body.get("typeId"), body.get("type")));
        o.setImageUrl(toStr(body.get("image_url"), body.get("imageUrl"), body.get("picture")));
        o.setQuestion(toStr(body.get("question")));
        o.setIsMulti(toBool(body.get("is_multi"), body.get("isMulti"), body.get("multi")));
        Object opts = body.get("options");
        if (opts instanceof List) {
            o.setOptions(String.join("|", ((List<Object>) opts).stream().map(Object::toString).toList()));
        } else if (opts != null) {
            o.setOptions(opts.toString());
        }
        o.setAnswer(toStr(body.get("answer")));
        o.setAnalysis(toStr(body.get("analysis")));
        return o;
    }

    private int toInt(Object... values) {
        for (Object v : values) {
            if (v == null) continue;
            try { return Integer.parseInt(String.valueOf(v)); } catch (Exception ignored) {}
        }
        return 0;
    }

    private String toStr(Object... values) {
        for (Object v : values) {
            if (v != null) return v.toString();
        }
        return "";
    }

    private boolean toBool(Object... values) {
        for (Object v : values) {
            if (v == null) continue;
            String s = String.valueOf(v).trim();
            if ("1".equals(s) || "true".equalsIgnoreCase(s)) return true;
            if ("0".equals(s) || "false".equalsIgnoreCase(s)) return false;
        }
        return false;
    }
}
