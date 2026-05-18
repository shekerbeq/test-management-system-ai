package kz.testmanagement.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.testmanagement.test.entity.Question;
import kz.testmanagement.test.entity.TestConfig;
import kz.testmanagement.test.entity.TestSession;
import kz.testmanagement.test.entity.UserAnswer;
import kz.testmanagement.test.repository.QuestionRepository;
import kz.testmanagement.test.repository.TestConfigRepository;
import kz.testmanagement.test.repository.TestSessionRepository;
import kz.testmanagement.test.repository.UserAnswerRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class TestSessionServiceTest {

    @Test
    void startTestReturnsExistingInProgressSession() {
        TestSessionRepository sessionRepository = mock(TestSessionRepository.class);
        UserAnswerRepository answerRepository = mock(UserAnswerRepository.class);
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        TestConfigRepository testConfigRepository = mock(TestConfigRepository.class);
        TestSessionService service = new TestSessionService(
                sessionRepository,
                answerRepository,
                questionRepository,
                testConfigRepository,
                new ObjectMapper()
        );

        TestConfig config = TestConfig.builder()
                .id(7L)
                .status("PUBLISHED")
                .timerMin(30)
                .build();
        TestSession existing = TestSession.builder()
                .id(11L)
                .testConfig(config)
                .studentId(3L)
                .status("IN_PROGRESS")
                .startedAt(LocalDateTime.now())
                .build();

        when(testConfigRepository.findById(7L)).thenReturn(Optional.of(config));
        when(sessionRepository.findFirstByTestConfig_IdAndStudentIdAndStatusOrderByStartedAtDesc(7L, 3L, "IN_PROGRESS"))
                .thenReturn(Optional.of(existing));

        TestSession result = service.startTest(7L, 3L);

        assertSame(existing, result);
        verify(sessionRepository, never()).save(any(TestSession.class));
    }

    @Test
    void finishTestScoresMultipleChoiceOnlyWhenAllSelectedIndicesMatch() {
        TestSessionRepository sessionRepository = mock(TestSessionRepository.class);
        UserAnswerRepository answerRepository = mock(UserAnswerRepository.class);
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        TestConfigRepository testConfigRepository = mock(TestConfigRepository.class);
        TestSessionService service = new TestSessionService(
                sessionRepository,
                answerRepository,
                questionRepository,
                testConfigRepository,
                new ObjectMapper()
        );

        TestConfig config = TestConfig.builder().id(7L).timerMin(30).build();
        TestSession session = TestSession.builder()
                .id(11L)
                .testConfig(config)
                .studentId(3L)
                .status("IN_PROGRESS")
                .startedAt(LocalDateTime.now())
                .build();
        Question question = Question.builder()
                .id(21L)
                .testConfig(config)
                .correctIndicesJson("[0,2]")
                .openQuestion(false)
                .build();
        UserAnswer answer = UserAnswer.builder()
                .id(31L)
                .testSession(session)
                .question(question)
                .selectedIndicesJson("[0,2]")
                .build();

        when(sessionRepository.findById(11L)).thenReturn(Optional.of(session));
        when(answerRepository.findByTestSession_Id(11L)).thenReturn(List.of(answer));
        when(answerRepository.save(any(UserAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionRepository.save(any(TestSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TestSession result = service.finishTest(11L);

        assertEquals(1, result.getScore());
        assertEquals(Boolean.TRUE, answer.getIsCorrect());
    }
}
