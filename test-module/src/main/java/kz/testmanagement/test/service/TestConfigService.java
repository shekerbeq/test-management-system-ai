package kz.testmanagement.test.service;

import kz.testmanagement.test.entity.Question;
import kz.testmanagement.test.entity.TestConfig;
import kz.testmanagement.test.repository.QuestionRepository;
import kz.testmanagement.test.repository.TestConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestConfigService {

    private final TestConfigRepository testConfigRepository;
    private final QuestionRepository questionRepository;   // <-- добавил

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

    @Transactional
    public void deleteById(Long id) {
        // сначала удаляем все вопросы этого теста
        List<Question> questions = questionRepository.findByTestConfigId(id);
        questionRepository.deleteAll(questions);
        // потом сам тест
        testConfigRepository.deleteById(id);
    }
}