package kz.testmanagement.test.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.testmanagement.core.dto.QuestionDto;
import kz.testmanagement.test.entity.Question;
import kz.testmanagement.test.entity.TestConfig;
import kz.testmanagement.test.repository.QuestionRepository;
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
@PreAuthorize("hasRole('TEACHER')")   // все методы по умолчанию только для учителя
public class TestConfigController {

    private final TestConfigService testConfigService;
    private final TestCreationService testCreationService;
    private final AnalyticsService analyticsService;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---------- учительские методы ----------
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

    // ---------- методы, доступные студентам и учителям ----------
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public ResponseEntity<List<TestConfig>> getAllTests() {
        return ResponseEntity.ok(testConfigService.findAll());
    }

    @GetMapping("/{id}/questions")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public ResponseEntity<List<QuestionDto>> getTestQuestions(@PathVariable Long id) {
        List<Question> questions = questionRepository.findByTestConfigId(id);
        List<QuestionDto> dtos = questions.stream()
                .map(q -> {
                    List<String> options = parseOptions(q.getOptionsJson());
                    return new QuestionDto(q.getText(), options, q.getCorrectIndex());
                })
                .toList();
        return ResponseEntity.ok(dtos);
    }

    private List<String> parseOptions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}