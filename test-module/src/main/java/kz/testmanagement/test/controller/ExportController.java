package kz.testmanagement.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.testmanagement.test.entity.TestConfig;
import kz.testmanagement.test.service.AnalyticsService;
import kz.testmanagement.test.service.TestConfigService;
import kz.testmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class ExportController {

    private final AnalyticsService analyticsService;
    private final TestConfigService testConfigService;
    private final UserService userService;

    @Operation(summary = "Тест нәтижелерін CSV форматында жүктеу")
    @GetMapping("/{id}/export.csv")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<String> exportCsv(@PathVariable Long id, Authentication authentication) {
        if (!canManage(id, authentication)) {
            return ResponseEntity.notFound().build();
        }
        var stats = analyticsService.getTestStatistics(id);
        String csv = "testConfigId,totalAttempts,completedAttempts,averageScore,maxScore,minScore\n"
                + stats.get("testConfigId") + "," + stats.get("totalAttempts") + ","
                + stats.get("completedAttempts") + "," + stats.get("averageScore") + ","
                + stats.get("maxScore") + "," + stats.get("minScore") + "\n\n"
                + "studentId,score,finishedAt\n"
                + analyticsService.getRating(id).stream()
                .map(row -> row.get("studentId") + "," + row.get("score") + "," + row.get("finishedAt"))
                .collect(java.util.stream.Collectors.joining("\n"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test-" + id + "-stats.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @Operation(summary = "Тест нәтижелерін PDF форматында жүктеу")
    @GetMapping("/{id}/export.pdf")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id, Authentication authentication) {
        if (!canManage(id, authentication)) {
            return ResponseEntity.notFound().build();
        }
        var stats = analyticsService.getTestStatistics(id);
        String text = "Test statistics " + stats;
        String pdf = """
                %PDF-1.4
                1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj
                2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj
                3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj
                4 0 obj << /Length %d >> stream
                BT /F1 12 Tf 50 780 Td (%s) Tj ET
                endstream endobj
                5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj
                trailer << /Root 1 0 R >>
                %%EOF
                """.formatted(text.length() + 32, text.replace("(", "[").replace(")", "]"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test-" + id + "-stats.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Operation(summary = "Тест рейтингі")
    @GetMapping("/{id}/rating")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<?> rating(@PathVariable Long id, Authentication authentication) {
        if (!canManage(id, authentication)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(analyticsService.getRating(id));
    }

    private boolean canManage(Long testId, Authentication authentication) {
        return testConfigService.findById(testId)
                .filter(test -> isAdmin(authentication) || owns(test, authentication))
                .isPresent();
    }

    private boolean owns(TestConfig test, Authentication authentication) {
        Long currentUserId = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
        return test.getCreatorId() != null && test.getCreatorId().equals(currentUserId);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
