package kz.testmanagement.result.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.testmanagement.result.service.ResultQueryService;
import kz.testmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultQueryService resultQueryService;
    private final UserService userService;

    @Operation(summary = "Тест сессиясының нәтижесін алу")
    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public ResponseEntity<?> getSessionResult(@PathVariable Long sessionId, Authentication authentication) {
        Map<String, Object> result = resultQueryService.getSessionResult(sessionId);
        if (!canRead(result, authentication)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    private boolean canRead(Map<String, Object> result, Authentication authentication) {
        Long currentUserId = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return true;
        }
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_TEACHER".equals(authority.getAuthority()))) {
            return currentUserId.equals(result.get("creatorId"));
        }
        return currentUserId.equals(result.get("studentId"));
    }
}
