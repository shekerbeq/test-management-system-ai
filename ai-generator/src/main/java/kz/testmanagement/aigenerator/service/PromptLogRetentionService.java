package kz.testmanagement.aigenerator.service;

import kz.testmanagement.aigenerator.repository.PromptLogRepository;
import kz.testmanagement.core.entity.PromptLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PromptLogRetentionService {

    private final PromptLogRepository promptLogRepository;

    @Value("${llm.provider:mock}")
    private String provider;

    @Value("${llm.model:${deepseek.model:mock}}")
    private String model;

    public void record(String topic, String prompt) {
        promptLogRepository.save(PromptLog.builder()
                .topic(topic)
                .provider(provider)
                .model(model)
                .promptHash(hash(prompt))
                .build());
    }

    @Scheduled(cron = "0 20 3 * * *")
    public void cleanupOldPromptLogs() {
        promptLogRepository.deleteByCreatedAtBefore(LocalDateTime.now().minusDays(30));
    }

    private String hash(String prompt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest((prompt == null ? "" : prompt).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return "unavailable";
        }
    }
}
