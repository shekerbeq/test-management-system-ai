package kz.testmanagement.test.repository;

import kz.testmanagement.test.entity.TestConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestConfigRepository extends JpaRepository<TestConfig, Long> {
    List<TestConfig> findByCreatorId(Long creatorId);
}