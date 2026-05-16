package kz.testmanagement.test.service;

import kz.testmanagement.test.entity.TestConfig;
import kz.testmanagement.test.repository.TestConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestConfigService {

    private final TestConfigRepository testConfigRepository;

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

    public void deleteById(Long id) {
        testConfigRepository.deleteById(id);
    }
}
