package kz.testmanagement.test.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.testmanagement.test.dto.AnswerReviewDto;
import kz.testmanagement.test.entity.Question;
import kz.testmanagement.test.entity.TestSession;
import kz.testmanagement.test.entity.UserAnswer;
import kz.testmanagement.test.repository.QuestionRepository;
import kz.testmanagement.test.repository.TestConfigRepository;
import kz.testmanagement.test.repository.TestSessionRepository;
import kz.testmanagement.test.repository.UserAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TestSessionService {

    private final TestSessionRepository sessionRepository;
    private final UserAnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final TestConfigRepository testConfigRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public TestSession startTest(Long testConfigId, Long studentId) {
        var config = testConfigRepository.findById(testConfigId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        if (!"PUBLISHED".equals(config.getStatus())) {
            throw new RuntimeException("Test is not published");
        }

        var existingSession = sessionRepository
                .findFirstByTestConfig_IdAndStudentIdAndStatusOrderByStartedAtDesc(testConfigId, studentId, "IN_PROGRESS");
        if (existingSession.isPresent()) {
            TestSession session = existingSession.get();
            enforceTimer(session);
            return session;
        }

        TestSession session = TestSession.builder()
                .testConfig(config)
                .studentId(studentId)
                .status("IN_PROGRESS")
                .startedAt(LocalDateTime.now())
                .build();
        return sessionRepository.save(session);
    }

    @Transactional
    public UserAnswer submitAnswer(Long sessionId, Long questionId, Integer selectedIndex, String openAnswer) {
        List<Integer> selectedIndices = selectedIndex == null ? List.of() : List.of(selectedIndex);
        return submitAnswer(sessionId, questionId, selectedIndex, selectedIndices, openAnswer);
    }

    @Transactional
    public UserAnswer submitAnswer(Long sessionId, Long questionId, Integer selectedIndex,
                                   List<Integer> selectedIndices, String openAnswer) {
        TestSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new RuntimeException("Test is already finished or closed");
        }
        enforceTimer(session);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        if (question.getTestConfig() == null || !question.getTestConfig().getId().equals(session.getTestConfigId())) {
            throw new RuntimeException("Question does not belong to this session test");
        }

        List<Integer> normalizedIndices = normalizeIndices(selectedIndices, selectedIndex);
        UserAnswer answer = UserAnswer.builder()
                .testSession(session)
                .question(question)
                .selectedIndex(normalizedIndices.isEmpty() ? null : normalizedIndices.get(0))
                .selectedIndicesJson(writeIndices(normalizedIndices))
                .openAnswer(openAnswer)
                .build();
        return answerRepository.save(answer);
    }

    @Transactional
    public TestSession finishTest(Long sessionId) {
        TestSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new RuntimeException("Test is already finished");
        }
        enforceTimer(session);

        List<UserAnswer> answers = answerRepository.findByTestSession_Id(sessionId);
        int correctCount = 0;

        for (UserAnswer answer : answers) {
            Question question = answer.getQuestion();
            if (!question.isOpenQuestion() && isClosedAnswerCorrect(question, answer)) {
                answer.setIsCorrect(true);
                correctCount++;
            } else if (!question.isOpenQuestion()) {
                answer.setIsCorrect(false);
            } else {
                answer.setIsCorrect(null);
            }
            answerRepository.save(answer);
        }

        session.setStatus("COMPLETED");
        session.setFinishedAt(LocalDateTime.now());
        session.setScore(correctCount);
        return sessionRepository.save(session);
    }

    public TestSession getResult(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    public List<TestSession> getStudentSessions(Long studentId) {
        return sessionRepository.findByStudentId(studentId);
    }

    public List<UserAnswer> getAnswers(Long sessionId) {
        return answerRepository.findByTestSession_Id(sessionId);
    }

    public List<AnswerReviewDto> getAnswerReview(Long sessionId) {
        return answerRepository.findByTestSession_Id(sessionId).stream()
                .map(answer -> {
                    Question question = answer.getQuestion();
                    List<Integer> selected = readIndices(answer.getSelectedIndicesJson());
                    if (selected.isEmpty() && answer.getSelectedIndex() != null) {
                        selected = List.of(answer.getSelectedIndex());
                    }
                    List<Integer> correct = question == null ? List.of() : readIndices(question.getCorrectIndicesJson());
                    if (correct.isEmpty() && question != null && question.getCorrectIndex() != null) {
                        correct = List.of(question.getCorrectIndex());
                    }
                    return new AnswerReviewDto(
                            answer.getId(),
                            question == null ? null : question.getId(),
                            question == null ? "" : question.getText(),
                            question == null ? List.of() : readStrings(question.getOptionsJson()),
                            selected,
                            correct,
                            answer.getOpenAnswer(),
                            answer.getIsCorrect(),
                            question != null && question.isOpenQuestion()
                    );
                })
                .toList();
    }

    public UserAnswer getAnswer(Long answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
    }

    @Transactional
    public UserAnswer gradeAnswer(Long answerId, boolean correct) {
        UserAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        answer.setIsCorrect(correct);
        UserAnswer saved = answerRepository.save(answer);
        recalculateScore(answer.getTestSession().getId());
        return saved;
    }

    @Transactional
    public UserAnswer aiGradeOpenAnswer(Long answerId) {
        UserAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        String value = answer.getOpenAnswer() == null ? "" : answer.getOpenAnswer().trim();
        answer.setIsCorrect(value.length() >= 20);
        UserAnswer saved = answerRepository.save(answer);
        recalculateScore(answer.getTestSession().getId());
        return saved;
    }

    private void recalculateScore(Long sessionId) {
        TestSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        int score = (int) answerRepository.findByTestSession_Id(sessionId).stream()
                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();
        session.setScore(score);
        sessionRepository.save(session);
    }

    private boolean isClosedAnswerCorrect(Question question, UserAnswer answer) {
        Set<Integer> expected = new LinkedHashSet<>(readIndices(question.getCorrectIndicesJson()));
        if (expected.isEmpty() && question.getCorrectIndex() != null) {
            expected.add(question.getCorrectIndex());
        }

        Set<Integer> actual = new LinkedHashSet<>(readIndices(answer.getSelectedIndicesJson()));
        if (actual.isEmpty() && answer.getSelectedIndex() != null) {
            actual.add(answer.getSelectedIndex());
        }

        return !expected.isEmpty() && Objects.equals(expected, actual);
    }

    private List<Integer> normalizeIndices(List<Integer> selectedIndices, Integer selectedIndex) {
        LinkedHashSet<Integer> normalized = new LinkedHashSet<>();
        if (selectedIndices != null) {
            selectedIndices.stream()
                    .filter(Objects::nonNull)
                    .filter(index -> index >= 0)
                    .forEach(normalized::add);
        }
        if (normalized.isEmpty() && selectedIndex != null && selectedIndex >= 0) {
            normalized.add(selectedIndex);
        }
        return new ArrayList<>(normalized);
    }

    private String writeIndices(List<Integer> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<Integer> readIndices(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<String> readStrings(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private void enforceTimer(TestSession session) {
        int timerMin = session.getTestConfig().getTimerMin();
        if (timerMin <= 0 || session.getStartedAt() == null) {
            return;
        }
        if (LocalDateTime.now().isAfter(session.getStartedAt().plusMinutes(timerMin))) {
            session.setStatus("COMPLETED");
            session.setFinishedAt(LocalDateTime.now());
            session.setScore(0);
            sessionRepository.save(session);
            throw new RuntimeException("Test time is over");
        }
    }
}
