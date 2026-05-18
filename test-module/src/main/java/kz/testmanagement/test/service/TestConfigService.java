package kz.testmanagement.test.service;

import kz.testmanagement.test.entity.Question;
import kz.testmanagement.test.entity.TestConfig;
import kz.testmanagement.test.entity.TestSession;
import kz.testmanagement.test.repository.QuestionRepository;
import kz.testmanagement.test.repository.TestConfigRepository;
import kz.testmanagement.test.repository.TestSessionRepository;
import kz.testmanagement.test.repository.UserAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestConfigService {

    private final TestConfigRepository testConfigRepository;
    private final QuestionRepository questionRepository;
    private final TestSessionRepository sessionRepository;
    private final UserAnswerRepository answerRepository;

    public TestConfig save(TestConfig config) {
        return testConfigRepository.save(config);
    }

    public Optional<TestConfig> findById(Long id) {
        return testConfigRepository.findById(id);
    }

    public List<TestConfig> findByCreatorId(Long creatorId) {
        return testConfigRepository.findByCreatorId(creatorId);
    }

    public List<TestConfig> findAll() {
        return testConfigRepository.findAll();
    }

    public List<TestConfig> findPublished() {
        return testConfigRepository.findByStatus("PUBLISHED");
    }

    public Optional<TestConfig> findByAccessCode(String accessCode) {
        return testConfigRepository.findByAccessCode(accessCode);
    }

    @Transactional
    public void deleteById(Long id) {
        List<TestSession> sessions = sessionRepository.findByTestConfig_Id(id);
        for (TestSession session : sessions) {
            answerRepository.deleteByTestSession_Id(session.getId());
        }

        List<Question> questions = questionRepository.findByTestConfig_Id(id);
        for (Question question : questions) {
            answerRepository.deleteByQuestion_Id(question.getId());
        }

        sessionRepository.deleteByTestConfig_Id(id);
        questionRepository.deleteAll(questions);
        testConfigRepository.deleteById(id);
    }

    @Transactional
    public long deleteAllTests() {
        long count = testConfigRepository.count();
        for (TestConfig test : testConfigRepository.findAll()) {
            deleteById(test.getId());
        }
        return count;
    }
}
