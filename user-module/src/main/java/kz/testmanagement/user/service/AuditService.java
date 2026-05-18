package kz.testmanagement.user.service;

import kz.testmanagement.core.entity.AuditLog;
import kz.testmanagement.user.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(String userEmail, String action, String path, String ip) {
        auditLogRepository.save(AuditLog.builder()
                .userEmail(userEmail)
                .action(action)
                .path(path)
                .ip(ip)
                .build());
    }
}
