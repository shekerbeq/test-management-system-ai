package kz.testmanagement.test.controller;

import kz.testmanagement.test.entity.TestSession;
import kz.testmanagement.test.entity.UserAnswer;
import kz.testmanagement.test.service.TestSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestSessionController {

    private final TestSessionService sessionService;

    // Студент начинает тест
    @PostMapping("/{testConfigId}/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<TestSession> startTest(@PathVariable Long testConfigId,
                                                 @RequestParam Long studentId) {
        return ResponseEntity.ok(sessionService.startTest(testConfigId, studentId));
    }

    // Студент отправляет ответ на вопрос
    @PostMapping("/sessions/{sessionId}/answers")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<UserAnswer> submitAnswer(@PathVariable Long sessionId,
                                                   @RequestParam Long questionId,
                                                   @RequestParam(required = false) Integer selectedIndex,
                                                   @RequestParam(required = false) String openAnswer) {
        return ResponseEntity.ok(sessionService.submitAnswer(sessionId, questionId, selectedIndex, openAnswer));
    }

    // Студент завершает тест
    @PostMapping("/sessions/{sessionId}/finish")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<TestSession> finishTest(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.finishTest(sessionId));
    }

    // Получить результат сессии (доступно и студенту, и преподавателю)
    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public ResponseEntity<TestSession> getResult(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.getResult(sessionId));
    }

    // Преподаватель может посмотреть все ответы сессии
    @GetMapping("/sessions/{sessionId}/answers")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<UserAnswer>> getAnswers(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.getAnswers(sessionId));
    }
}