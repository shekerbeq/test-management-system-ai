package kz.testmanagement.aigenerator.service;

import kz.testmanagement.aigenerator.repository.AiUsageLogRepository;
import kz.testmanagement.core.dto.QuestionDto;
import kz.testmanagement.core.entity.AiUsageLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CostTracker {

    private final AiUsageLogRepository aiUsageLogRepository;

    @Value("${llm.provider:mock}")
    private String provider;

    @Value("${llm.model:${deepseek.model:mock}}")
    private String model;

    public void record(String topic, String prompt, List<QuestionDto> generated) {
        int tokens = estimateTokens(prompt, generated);
        aiUsageLogRepository.save(AiUsageLog.builder()
                .provider(provider)
                .model(model)
                .topic(topic)
                .tokensUsed(tokens)
                .costEstimate(tokens * 0.000001)
                .build());
    }

    private int estimateTokens(String prompt, List<QuestionDto> generated) {
        int promptTokens = Math.max(1, (prompt == null ? 0 : prompt.length()) / 4);
        int resultTokens = Math.max(1, (generated == null ? 0 : generated.toString().length()) / 4);
        return promptTokens + resultTokens;
    }
}
