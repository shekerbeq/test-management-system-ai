package kz.testmanagement.test.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import kz.testmanagement.core.dto.QuestionDto;
import kz.testmanagement.test.entity.Question;
import kz.testmanagement.test.entity.TestConfig;
import kz.testmanagement.test.repository.QuestionRepository;
import kz.testmanagement.test.service.AnalyticsService;
import kz.testmanagement.test.service.TestConfigService;
import kz.testmanagement.test.service.TestCreationService;
import kz.testmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
public class TestConfigController {

    private final TestConfigService testConfigService;
    private final TestCreationService testCreationService;
    private final AnalyticsService analyticsService;
    private final QuestionRepository questionRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---------- учительские методы ----------
    @PostMapping
    @Operation(summary = "Тест конфигурациясын жасау")
    public ResponseEntity<TestConfig> createTest(@Valid @RequestBody TestConfig config, Authentication authentication) {
        config.setCreatorId(currentUserId(authentication));
        return ResponseEntity.ok(testConfigService.save(config));
    }

    @PostMapping("/generate-and-save")
    @Operation(summary = "AI preview сұрақтарын тест ретінде сақтау")
    public ResponseEntity<TestConfig> generateAndSave(@Valid @RequestBody TestConfig config, Authentication authentication) {
        config.setCreatorId(currentUserId(authentication));
        return ResponseEntity.ok(testCreationService.createTestWithQuestions(config));
    }

    @GetMapping("/my")
    @Operation(summary = "Оқытушының өз тесттерін алу")
    public ResponseEntity<List<TestConfig>> getMyTests(Authentication authentication) {
        return ResponseEntity.ok(testConfigService.findByCreatorId(currentUserId(authentication)));
    }

    @GetMapping("/admin/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Админский обзор всех тестов и результатов")
    public ResponseEntity<List<Map<String, Object>>> adminOverview() {
        List<Map<String, Object>> rows = testConfigService.findAll().stream()
                .map(test -> {
                    Map<String, Object> stats = analyticsService.getTestStatistics(test.getId());
                    List<Map<String, Object>> rating = analyticsService.getRating(test.getId());
                    int maxScore = Math.max(test.getCount(), 1);
                    long total = ((Number) stats.get("totalAttempts")).longValue();
                    long completed = ((Number) stats.get("completedAttempts")).longValue();
                    long passed = rating.stream()
                            .filter(row -> ((Number) row.get("score")).doubleValue() * 100 / maxScore >= 70)
                            .count();
                    double averagePercent = ((Number) stats.get("averageScore")).doubleValue() * 100 / maxScore;
                    double passRate = completed == 0 ? 0 : passed * 100.0 / completed;
                    double completionRate = total == 0 ? 0 : completed * 100.0 / total;
                    Map<String, Object> row = new java.util.LinkedHashMap<>();
                    row.put("id", test.getId());
                    row.put("title", test.getTitle());
                    row.put("topic", test.getTopic());
                    row.put("status", test.getStatus());
                    row.put("language", test.getLanguage());
                    row.put("questionCount", test.getCount());
                    row.put("accessCode", test.getAccessCode());
                    row.put("createdAt", test.getCreatedAt());
                    row.put("teacherId", test.getCreatorId());
                    row.put("teacher", userService.findById(test.getCreatorId())
                            .map(user -> user.getFullName() + " (" + user.getEmail() + ")")
                            .orElse("Teacher #" + test.getCreatorId()));
                    row.put("totalAttempts", total);
                    row.put("completedAttempts", completed);
                    row.put("averageScore", stats.get("averageScore"));
                    row.put("averagePercent", Math.round(averagePercent));
                    row.put("passRate", Math.round(passRate));
                    row.put("completionRate", Math.round(completionRate));
                    row.put("passedCount", passed);
                    return row;
                })
                .toList();
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Тест конфигурациясын алу")
    public ResponseEntity<TestConfig> getTest(@PathVariable Long id, Authentication authentication) {
        return testConfigService.findById(id)
                .filter(test -> canManage(test, authentication))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{accessCode}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    @Operation(summary = "Код арқылы жарияланған тестті табу")
    public ResponseEntity<TestConfig> getByAccessCode(@PathVariable String accessCode) {
        return testConfigService.findByAccessCode(accessCode)
                .filter(test -> "PUBLISHED".equals(test.getStatus()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @Operation(summary = "Тест статистикасын алу")
    public ResponseEntity<Map<String, Object>> getStatistics(@PathVariable Long id, Authentication authentication) {
        if (testConfigService.findById(id).filter(test -> canManage(test, authentication)).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(analyticsService.getTestStatistics(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Тест конфигурациясын жаңарту")
    public ResponseEntity<TestConfig> updateTest(@PathVariable Long id, @Valid @RequestBody TestConfig updated,
                                                 Authentication authentication) {
        return testConfigService.findById(id)
                .filter(test -> canManage(test, authentication))
                .map(test -> {
                    test.setTitle(updated.getTitle());
                    test.setTopic(updated.getTopic());
                    test.setDifficulty(updated.getDifficulty());
                    test.setQuestionType(updated.getQuestionType());
                    test.setCount(updated.getCount());
                    test.setTimerMin(updated.getTimerMin());
                    test.setLanguage(updated.getLanguage());
                    test.setStatus(updated.getStatus());
                    if (updated.getPreviewQuestions() != null) {
                        return ResponseEntity.ok(testCreationService.replaceQuestions(test, updated.getPreviewQuestions()));
                    }
                    return ResponseEntity.ok(testConfigService.save(test));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/copy")
    @Operation(summary = "Скопировать тест вместе с вопросами")
    public ResponseEntity<TestConfig> copyTest(@PathVariable Long id, Authentication authentication) {
        return testConfigService.findById(id)
                .filter(test -> canManage(test, authentication))
                .map(test -> ResponseEntity.ok(testCreationService.copyTest(test, currentUserId(authentication))))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Тестті жою")
    public ResponseEntity<Void> deleteTest(@PathVariable Long id, Authentication authentication) {
        if (testConfigService.findById(id).filter(test -> canManage(test, authentication)).isPresent()) {
            testConfigService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить все тесты, вопросы, сессии и ответы")
    public ResponseEntity<Map<String, Object>> deleteAllTests() {
        return ResponseEntity.ok(Map.of("deleted", testConfigService.deleteAllTests()));
    }

    // ---------- методы, доступные студентам и учителям ----------
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    @Operation(summary = "Қолжетімді тесттерді алу")
    public ResponseEntity<List<TestConfig>> getAllTests(Authentication authentication) {
        boolean manager = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_TEACHER".equals(authority.getAuthority()) || "ROLE_ADMIN".equals(authority.getAuthority()));
        return ResponseEntity.ok(manager ? testConfigService.findAll() : testConfigService.findPublished());
    }

    @GetMapping("/{id}/questions")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    @Operation(summary = "Тест сұрақтарын алу")
    public ResponseEntity<List<QuestionDto>> getTestQuestions(@PathVariable Long id, Authentication authentication) {
        boolean manager = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_TEACHER".equals(authority.getAuthority()) || "ROLE_ADMIN".equals(authority.getAuthority()));
        if (manager && testConfigService.findById(id).filter(test -> canManage(test, authentication)).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!manager && testConfigService.findById(id).filter(test -> "PUBLISHED".equals(test.getStatus())).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Question> questions = questionRepository.findByTestConfig_Id(id);
        List<QuestionDto> dtos = questions.stream()
                .map(q -> {
                    List<String> options = parseOptions(q.getOptionsJson());
                    return new QuestionDto(q.getId(), q.getText(), options, parseCorrectIndices(q), q.getCorrectIndex(), q.isOpenQuestion());
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

    private List<Integer> parseCorrectIndices(Question question) {
        if (question.getCorrectIndicesJson() != null) {
            try {
                return objectMapper.readValue(question.getCorrectIndicesJson(), new TypeReference<List<Integer>>() {});
            } catch (Exception ignored) {
                // use legacy single correct index below
            }
        }
        return question.getCorrectIndex() == null ? List.of() : List.of(question.getCorrectIndex());
    }

    private Long currentUserId(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Пайдаланушы табылмады"))
                .getId();
    }

    private boolean canManage(TestConfig test, Authentication authentication) {
        Authentication auth = authentication == null
                ? org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                : authentication;
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return admin || (test.getCreatorId() != null && test.getCreatorId().equals(currentUserId(auth)));
    }
}
