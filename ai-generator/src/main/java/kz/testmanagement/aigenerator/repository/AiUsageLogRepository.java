package kz.testmanagement.aigenerator.repository;

import kz.testmanagement.core.entity.AiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {
}
