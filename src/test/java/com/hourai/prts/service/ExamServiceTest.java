package com.hourai.prts.service;

import com.hourai.prts.dto.ExamSubmissionResultDTO;
import com.hourai.prts.entity.ExamDetail;
import com.hourai.prts.entity.ExamRecord;
import com.hourai.prts.entity.Question;
import com.hourai.prts.repository.ExamDetailRepository;
import com.hourai.prts.repository.ExamRecordRepository;
import com.hourai.prts.repository.QuestionRepository;
import com.hourai.prts.repository.UserAnswerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExamServiceTest {

    @Test
    void submitCalculatesScoreAndPersistsSummaryAnswersAndDetails() {
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        ExamRecordRepository recordRepository = mock(ExamRecordRepository.class);
        ExamDetailRepository detailRepository = mock(ExamDetailRepository.class);
        UserAnswerRepository answerRepository = mock(UserAnswerRepository.class);
        ExamService service = new ExamService(
                questionRepository, recordRepository, detailRepository, answerRepository);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question(1L, "2")));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(question(2L, "3")));
        when(recordRepository.save(any(ExamRecord.class))).thenAnswer(invocation -> {
            ExamRecord record = invocation.getArgument(0);
            record.setId(99L);
            return record;
        });

        ExamSubmissionResultDTO result = service.submitExam(
                7L, Map.of(1L, 2, 2L, 1), 45);

        assertEquals(new BigDecimal("50.00"), result.getScore());
        assertEquals(2, result.getTotalQuestions());
        assertEquals(1, result.getCorrectCount());
        assertEquals(2, result.getQuestions().size());
        verify(answerRepository, times(2)).save(any());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<ExamDetail>> detailsCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(detailRepository).saveAll(detailsCaptor.capture());
        List<ExamDetail> details = ((List<ExamDetail>) detailsCaptor.getValue());
        assertEquals(2, details.size());
        assertEquals(99L, details.get(0).getExamId());
    }

    @Test
    void nonexistentQuestionIsNotIncludedInScoreDenominator() {
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        ExamRecordRepository recordRepository = mock(ExamRecordRepository.class);
        ExamDetailRepository detailRepository = mock(ExamDetailRepository.class);
        UserAnswerRepository answerRepository = mock(UserAnswerRepository.class);
        ExamService service = new ExamService(
                questionRepository, recordRepository, detailRepository, answerRepository);
        when(questionRepository.findById(404L)).thenReturn(Optional.empty());
        when(recordRepository.save(any(ExamRecord.class))).thenAnswer(invocation -> {
            ExamRecord record = invocation.getArgument(0);
            record.setId(1L);
            return record;
        });

        ExamSubmissionResultDTO result = service.submitExam(7L, Map.of(404L, 1), null);

        assertEquals(0, result.getTotalQuestions());
        assertEquals(BigDecimal.ZERO, result.getScore());
    }

    private Question question(Long id, String answer) {
        Question question = new Question();
        question.setId(id);
        question.setAnswer(answer);
        question.setAnalysis("analysis");
        return question;
    }
}
