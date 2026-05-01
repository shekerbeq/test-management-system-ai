package kz.testmanagement.test.service;

import kz.testmanagement.test.entity.*;
import kz.testmanagement.test.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestSessionService {

    private final TestSessionRepository sessionRepository;
    private final UserAnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final TestConfigRepository testConfigRepository;

    // Начать новую попытку
    @Transactional
    public TestSession startTest(Long testConfigId, Long studentId) {
        TestConfig config = testConfigRepository.findById(testConfigId)
                .orElseThrow(() -> new RuntimeException("Тест табылмады"));

        TestSession session = TestSession.builder()
                .testConfig(config)
                .studentId(studentId)
                .status("IN_PROGRESS")
                .startedAt(LocalDateTime.now())
                .build();
        return sessionRepository.save(session);
    }

    // Сохранить ответ студента на вопрос
    @Transactional
    public UserAnswer submitAnswer(Long sessionId, Long questionId, Integer selectedIndex, String openAnswer) {
        TestSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Сессия табылмады"));

        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new RuntimeException("Тест аяқталған немесе жабылған");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Сұрақ табылмады"));

        UserAnswer answer = UserAnswer.builder()
                .testSession(session)
                .question(question)
                .selectedIndex(selectedIndex)
                .openAnswer(openAnswer)
                .build();
        return answerRepository.save(answer);
    }

    // Завершить тест и подсчитать баллы
    @Transactional
    public TestSession finishTest(Long sessionId) {
        TestSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Сессия табылмады"));

        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new RuntimeException("Тест бұрын аяқталған");
        }

        List<UserAnswer> answers = answerRepository.findByTestSessionId(sessionId);
        int correctCount = 0;

        for (UserAnswer answer : answers) {
            Question question = answer.getQuestion();
            if (question.getCorrectIndex() == answer.getSelectedIndex()) {
                answer.setIsCorrect(true);
                correctCount++;
            } else {
                answer.setIsCorrect(false);
            }
            answerRepository.save(answer);
        }

        session.setStatus("COMPLETED");
        session.setFinishedAt(LocalDateTime.now());
        session.setScore(correctCount);
        return sessionRepository.save(session);
    }

    // Получить результат сессии
    public TestSession getResult(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Сессия табылмады"));
    }

    // Получить все ответы сессии
    public List<UserAnswer> getAnswers(Long sessionId) {
        return answerRepository.findByTestSessionId(sessionId);
    }
}