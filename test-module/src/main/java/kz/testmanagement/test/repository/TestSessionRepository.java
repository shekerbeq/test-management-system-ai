package kz.testmanagement.test.repository;

import kz.testmanagement.test.entity.TestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TestSessionRepository extends JpaRepository<TestSession, Long> {
    List<TestSession> findByStudentId(Long studentId);
    List<TestSession> findByTestConfig_Id(Long testConfigId);
    Optional<TestSession> findFirstByTestConfig_IdAndStudentIdAndStatusOrderByStartedAtDesc(Long testConfigId, Long studentId, String status);
    void deleteByTestConfig_Id(Long testConfigId);
}
