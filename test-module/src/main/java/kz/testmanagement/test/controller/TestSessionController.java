package kz.testmanagement.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.testmanagement.test.entity.TestSession;
import kz.testmanagement.test.entity.UserAnswer;
import kz.testmanagement.test.service.TestSessionService;
import kz.testmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestSessionController {

    private final TestSessionService sessionService;
    private final UserService userService;

    @Operation(summary = "Студент тест сессиясын бастайды")
    @PostMapping("/{testConfigId}/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<TestSession> startTest(@PathVariable Long testConfigId,
                                                 @RequestParam(required = false) Long studentId,
                                                 Authentication authentication) {
        return ResponseEntity.ok(sessionService.startTest(testConfigId, currentUserId(authentication)));
    }

    @Operation(summary = "Студент жауап жібереді")
    @PostMapping("/sessions/{sessionId}/answers")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<UserAnswer> submitAnswer(@PathVariable Long sessionId,
                                                   @RequestParam Long questionId,
                                                   @RequestParam(required = false) Integer selectedIndex,
                                                   @RequestParam(required = false) List<Integer> selectedIndices,
                                                   @RequestParam(required = false) String openAnswer,
                                                   Authentication authentication) {
        if (!ownsSession(sessionId, authentication)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessionService.submitAnswer(sessionId, questionId, selectedIndex, selectedIndices, openAnswer));
    }

    @Operation(summary = "Студент тест сессиясын аяқтайды")
    @PostMapping("/sessions/{sessionId}/finish")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<TestSession> finishTest(@PathVariable Long sessionId, Authentication authentication) {
        if (!ownsSession(sessionId, authentication)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessionService.finishTest(sessionId));
    }

    @Operation(summary = "Сессия нәтижесін алу")
    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public ResponseEntity<TestSession> getResult(@PathVariable Long sessionId, Authentication authentication) {
        if (!canReadSession(sessionId, authentication)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessionService.getResult(sessionId));
    }

    @Operation(summary = "Студенттің өз сессияларын алу")
    @GetMapping("/sessions/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<TestSession>> getMySessions(@RequestParam(required = false) Long studentId,
                                                           Authentication authentication) {
        return ResponseEntity.ok(sessionService.getStudentSessions(currentUserId(authentication)));
    }

    @Operation(summary = "Оқытушы сессия жауаптарын қарайды")
    @GetMapping("/sessions/{sessionId}/answers")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<UserAnswer>> getAnswers(@PathVariable Long sessionId, Authentication authentication) {
        if (!canManageSession(sessionId, authentication)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessionService.getAnswers(sessionId));
    }

    @Operation(summary = "Сессия жауаптарын дұрыс жауаптарымен бірге қарау")
    @GetMapping("/sessions/{sessionId}/review")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public ResponseEntity<?> getAnswerReview(@PathVariable Long sessionId, Authentication authentication) {
        if (!canReadSession(sessionId, authentication)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessionService.getAnswerReview(sessionId));
    }

    @Operation(summary = "Ашық жауапты қолмен бағалау")
    @PostMapping("/answers/{answerId}/grade")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<UserAnswer> gradeAnswer(@PathVariable Long answerId,
                                                  @RequestParam boolean correct,
                                                  Authentication authentication) {
        if (!canManageAnswer(answerId, authentication)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessionService.gradeAnswer(answerId, correct));
    }

    @Operation(summary = "Ашық жауапты AI арқылы бағалау")
    @PostMapping("/answers/{answerId}/ai-grade")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<UserAnswer> aiGradeAnswer(@PathVariable Long answerId, Authentication authentication) {
        if (!canManageAnswer(answerId, authentication)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessionService.aiGradeOpenAnswer(answerId));
    }

    private boolean ownsSession(Long sessionId, Authentication authentication) {
        return sessionService.getResult(sessionId).getStudentId().equals(currentUserId(authentication));
    }

    private boolean canReadSession(Long sessionId, Authentication authentication) {
        boolean teacher = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_TEACHER".equals(authority.getAuthority()));
        return teacher ? canManageSession(sessionId, authentication) : ownsSession(sessionId, authentication);
    }

    private boolean canManageSession(Long sessionId, Authentication authentication) {
        TestSession session = sessionService.getResult(sessionId);
        Long creatorId = session.getTestConfig() == null ? null : session.getTestConfig().getCreatorId();
        return creatorId != null && creatorId.equals(currentUserId(authentication));
    }

    private boolean canManageAnswer(Long answerId, Authentication authentication) {
        UserAnswer answer = sessionService.getAnswer(answerId);
        Long sessionId = answer.getTestSession() == null ? null : answer.getTestSession().getId();
        return sessionId != null && canManageSession(sessionId, authentication);
    }

    private Long currentUserId(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }
}
