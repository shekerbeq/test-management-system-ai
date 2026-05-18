package kz.testmanagement.aigenerator.controller;

import kz.testmanagement.aigenerator.repository.AiUsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-usage")
@RequiredArgsConstructor
public class AiUsageController {

    private final AiUsageLogRepository repository;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> summary() {
        var logs = repository.findAll();
        int tokens = logs.stream().mapToInt(log -> log.getTokensUsed() == null ? 0 : log.getTokensUsed()).sum();
        double cost = logs.stream().mapToDouble(log -> log.getCostEstimate() == null ? 0 : log.getCostEstimate()).sum();
        return ResponseEntity.ok(Map.of("requests", logs.size(), "tokensUsed", tokens, "costEstimate", cost));
    }
}
