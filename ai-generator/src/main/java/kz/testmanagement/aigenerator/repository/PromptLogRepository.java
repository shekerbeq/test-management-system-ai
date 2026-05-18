package kz.testmanagement.aigenerator.repository;

import kz.testmanagement.core.entity.PromptLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PromptLogRepository extends JpaRepository<PromptLog, Long> {
    long deleteByCreatedAtBefore(LocalDateTime threshold);
}
