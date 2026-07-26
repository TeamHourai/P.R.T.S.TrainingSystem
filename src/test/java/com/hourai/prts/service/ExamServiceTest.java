package com.hourai.prts.service;

import com.hourai.prts.dto.ExamSubmissionResultDTO;
import com.hourai.prts.entity.ExamDetail;
import com.hourai.prts.entity.ExamPaper;
import com.hourai.prts.entity.ExamPaperQuestion;
import com.hourai.prts.entity.ExamRecord;
import com.hourai.prts.entity.Question;
import com.hourai.prts.repository.ExamDetailRepository;
import com.hourai.prts.repository.ExamPaperQuestionRepository;
import com.hourai.prts.repository.ExamPaperRepository;
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
        ExamPaperRepository paperRepository = mock(ExamPaperRepository.class);
        ExamPaperQuestionRepository paperQuestionRepository =
                mock(ExamPaperQuestionRepository.class);
        ExamService service = new ExamService(
                questionRepository, recordRepository, detailRepository, answerRepository,
                paperRepository, paperQuestionRepository);

        ExamPaper paper = activePaper(10L, 7L);
        when(paperRepository.findByIdAndUserId(10L, 7L)).thenReturn(Optional.of(paper));
        when(paperQuestionRepository.findByPaperIdOrderByPositionAsc(10L))
                .thenReturn(List.of(paperQuestion(10L, 1L, 1), paperQuestion(10L, 2L, 2)));
        when(questionRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(question(1L, "2"), question(2L, "3")));
        when(recordRepository.save(any(ExamRecord.class))).thenAnswer(invocation -> {
            ExamRecord record = invocation.getArgument(0);
            record.setId(99L);
            return record;
        });

        ExamSubmissionResultDTO result = service.submitExam(
                7L, 10L, Map.of(1L, 2, 2L, 1), 45);

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
    void unansweredQuestionsRemainInScoreDenominator() {
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        ExamRecordRepository recordRepository = mock(ExamRecordRepository.class);
        ExamDetailRepository detailRepository = mock(ExamDetailRepository.class);
        UserAnswerRepository answerRepository = mock(UserAnswerRepository.class);
        ExamPaperRepository paperRepository = mock(ExamPaperRepository.class);
        ExamPaperQuestionRepository paperQuestionRepository =
                mock(ExamPaperQuestionRepository.class);
        ExamService service = new ExamService(
                questionRepository, recordRepository, detailRepository, answerRepository,
                paperRepository, paperQuestionRepository);

        ExamPaper paper = activePaper(10L, 7L);
        when(paperRepository.findByIdAndUserId(10L, 7L)).thenReturn(Optional.of(paper));
        when(paperQuestionRepository.findByPaperIdOrderByPositionAsc(10L))
                .thenReturn(List.of(
                        paperQuestion(10L, 1L, 1),
                        paperQuestion(10L, 2L, 2),
                        paperQuestion(10L, 3L, 3)));
        when(questionRepository.findAllById(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(
                        question(1L, "2"),
                        question(2L, "3"),
                        question(3L, "1")));
        when(recordRepository.save(any(ExamRecord.class))).thenAnswer(invocation -> {
            ExamRecord record = invocation.getArgument(0);
            record.setId(1L);
            return record;
        });

        ExamSubmissionResultDTO result = service.submitExam(
                7L, 10L, Map.of(1L, 2), null);

        assertEquals(3, result.getTotalQuestions());
        assertEquals(1, result.getCorrectCount());
        assertEquals(new BigDecimal("33.33"), result.getScore());
        assertEquals(3, result.getQuestions().size());
        assertEquals(null, result.getQuestions().get(1).getSelectedAnswer());
        verify(answerRepository, times(1)).save(any());
    }

    private Question question(Long id, String answer) {
        Question question = new Question();
        question.setId(id);
        question.setAnswer(answer);
        question.setAnalysis("analysis");
        return question;
    }

    private ExamPaper activePaper(Long id, Long userId) {
        ExamPaper paper = new ExamPaper();
        paper.setId(id);
        paper.setUserId(userId);
        paper.setStatus(ExamPaper.STATUS_ACTIVE);
        return paper;
    }

    private ExamPaperQuestion paperQuestion(Long paperId, Long questionId, int position) {
        ExamPaperQuestion question = new ExamPaperQuestion();
        question.setPaperId(paperId);
        question.setQuestionId(questionId);
        question.setPosition(position);
        return question;
    }
}
