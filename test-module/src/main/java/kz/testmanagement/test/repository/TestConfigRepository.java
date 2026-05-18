package kz.testmanagement.test.repository;

import kz.testmanagement.test.entity.TestConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TestConfigRepository extends JpaRepository<TestConfig, Long> {
    List<TestConfig> findByCreatorId(Long creatorId);
    List<TestConfig> findByStatus(String status);
    Optional<TestConfig> findByAccessCode(String accessCode);
}
