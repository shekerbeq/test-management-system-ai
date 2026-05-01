package kz.testmanagement.test.repository;

import kz.testmanagement.test.entity.TestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestSessionRepository extends JpaRepository<TestSession, Long> {
    List<TestSession> findByStudentId(Long studentId);
    List<TestSession> findByTestConfigId(Long testConfigId);
}