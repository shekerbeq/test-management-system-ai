package kz.testmanagement.test.controller;

import kz.testmanagement.test.entity.TestConfig;
import kz.testmanagement.test.service.AnalyticsService;
import kz.testmanagement.test.service.TestConfigService;
import kz.testmanagement.test.service.TestCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TestConfigController {

    private final TestConfigService testConfigService;
    private final TestCreationService testCreationService;
    private final AnalyticsService analyticsService; // ← добавил

    @PostMapping
    public ResponseEntity<TestConfig> createTest(@RequestBody TestConfig config) {
        return ResponseEntity.ok(testConfigService.save(config));
    }

    @PostMapping("/generate-and-save")
    public ResponseEntity<TestConfig> generateAndSave(@RequestBody TestConfig config) {
        return ResponseEntity.ok(testCreationService.createTestWithQuestions(config));
    }

    @GetMapping("/my")
    public ResponseEntity<List<TestConfig>> getMyTests() {
        return ResponseEntity.ok(testConfigService.findByCreatorId(1L));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestConfig> getTest(@PathVariable Long id) {
        return testConfigService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getStatistics(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getTestStatistics(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestConfig> updateTest(@PathVariable Long id, @RequestBody TestConfig updated) {
        return testConfigService.findById(id)
                .map(test -> {
                    test.setTitle(updated.getTitle());
                    test.setTopic(updated.getTopic());
                    test.setDifficulty(updated.getDifficulty());
                    test.setQuestionType(updated.getQuestionType());
                    test.setCount(updated.getCount());
                    test.setTimerMin(updated.getTimerMin());
                    test.setLanguage(updated.getLanguage());
                    test.setStatus(updated.getStatus());
                    return ResponseEntity.ok(testConfigService.save(test));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTest(@PathVariable Long id) {
        if (testConfigService.findById(id).isPresent()) {
            testConfigService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}