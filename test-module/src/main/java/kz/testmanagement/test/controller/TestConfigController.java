package kz.testmanagement.test.controller;

import kz.testmanagement.test.entity.TestConfig;
import kz.testmanagement.test.service.TestConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TestConfigController {

    private final TestConfigService testConfigService;

    @PostMapping
    public ResponseEntity<TestConfig> createTest(@RequestBody TestConfig config) {
        // creatorId заполняется отдельно (можно из токена, пока захардкодим)
        // Пока для простоты оставим как есть, позже доработаем получение из аутентификации
        return ResponseEntity.ok(testConfigService.save(config));
    }

    @GetMapping("/my")
    public ResponseEntity<List<TestConfig>> getMyTests() {
        // Временно возвращаем все тесты. Позже сделаем фильтрацию по создателю.
        return ResponseEntity.ok(testConfigService.findByCreatorId(1L)); // заглушка
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestConfig> getTest(@PathVariable Long id) {
        return testConfigService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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