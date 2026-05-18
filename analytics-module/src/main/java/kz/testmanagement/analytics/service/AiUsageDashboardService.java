package kz.testmanagement.analytics.service;

import kz.testmanagement.aigenerator.repository.AiUsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiUsageDashboardService {

    private final AiUsageLogRepository aiUsageLogRepository;

    public Map<String, Object> dashboard() {
        var logs = aiUsageLogRepository.findAll();
        int tokens = logs.stream().mapToInt(log -> log.getTokensUsed() == null ? 0 : log.getTokensUsed()).sum();
        double cost = logs.stream().mapToDouble(log -> log.getCostEstimate() == null ? 0 : log.getCostEstimate()).sum();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("requests", logs.size());
        result.put("tokensUsed", tokens);
        result.put("costEstimate", cost);
        result.put("day", LocalDate.now());
        result.put("week", LocalDate.now().with(java.time.DayOfWeek.MONDAY));
        result.put("month", LocalDate.now().withDayOfMonth(1));
        return result;
    }
}
