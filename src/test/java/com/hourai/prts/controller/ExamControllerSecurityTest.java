package com.hourai.prts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hourai.prts.dto.ExamPaperQuestionDTO;
import com.hourai.prts.dto.ExamSubmissionResultDTO;
import com.hourai.prts.entity.Question;
import com.hourai.prts.repository.QuestionRepository;
import com.hourai.prts.repository.UserAnswerRepository;
import com.hourai.prts.repository.WrongQuestionVisibilityRepository;
import com.hourai.prts.service.ExamService;
import com.hourai.prts.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExamControllerSecurityTest {

    @Test
    void submitUsesAuthenticatedPrincipalInsteadOfClientUserId() {
        ExamService examService = mock(ExamService.class);
        ExamController controller = new ExamController(
                examService,
                mock(QuestionService.class),
                mock(UserAnswerRepository.class),
                mock(WrongQuestionVisibilityRepository.class),
                mock(QuestionRepository.class));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(42L);

        ExamSubmissionResultDTO result = new ExamSubmissionResultDTO();
        result.setExamId(100L);
        result.setScore(BigDecimal.valueOf(100));
        when(examService.submitExam(eq(42L), eq(9L), anyMap(), eq(30))).thenReturn(result);

        controller.submitExam(9L, "1:2", 30, authentication);

        verify(examService).submitExam(42L, 9L, Map.of(1L, 2), 30);
    }

    @Test
    void examPaperDtoDoesNotSerializeAnswerOrAnalysis() throws Exception {
        Question question = new Question();
        question.setId(1L);
        question.setType(1);
        question.setDifficulty(2);
        question.setQuestion("题目");
        question.setOptions("A|B|C|D");
        question.setAnswer("2");
        question.setAnalysis("解析");

        ExamPaperQuestionDTO dto = QuestionService.toExamPaperDTO(question);
        String json = new ObjectMapper().writeValueAsString(List.of(dto));

        assertFalse(json.contains("\"answer\""));
        assertFalse(json.contains("\"analysis\""));
    }
}
