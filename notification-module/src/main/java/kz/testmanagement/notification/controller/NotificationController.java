package kz.testmanagement.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.testmanagement.notification.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailNotificationService emailNotificationService;

    @Operation(summary = "Тест нәтижесі туралы email хабарлама жіберу")
    @PostMapping("/test-finished")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<Void> testFinished(@RequestBody Map<String, String> request) {
        emailNotificationService.sendTestFinished(
                request.get("email"),
                request.getOrDefault("testTitle", "Test"),
                request.getOrDefault("result", "Result is ready")
        );
        return ResponseEntity.accepted().build();
    }
}
