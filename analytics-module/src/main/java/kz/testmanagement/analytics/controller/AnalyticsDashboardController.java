package kz.testmanagement.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.testmanagement.analytics.service.AiUsageDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsDashboardController {

    private final AiUsageDashboardService aiUsageDashboardService;

    @Operation(summary = "AI token usage dashboard summary")
    @GetMapping("/ai-usage/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(aiUsageDashboardService.dashboard());
    }
}
