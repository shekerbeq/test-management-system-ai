package kz.testmanagement.user.controller;

import kz.testmanagement.core.entity.Role;
import kz.testmanagement.core.entity.User;
import kz.testmanagement.user.repository.AuditLogRepository;
import kz.testmanagement.user.repository.RefreshTokenRepository;
import kz.testmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @GetMapping("/users")
    public ResponseEntity<?> users() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(this::toUserRow)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пайдаланушы табылмады"));
        user.setRole(Role.valueOf(body.get("role")));
        return ResponseEntity.ok(toUserRow(userRepository.save(user)));
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<?> updateBlocked(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        user.setBlocked(Boolean.TRUE.equals(body.get("blocked")));
        if (user.isBlocked()) {
            refreshTokenRepository.deleteByUserId(id);
        }
        return ResponseEntity.ok(toUserRow(userRepository.save(user)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        refreshTokenRepository.deleteByUserId(id);
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users")
    public ResponseEntity<Map<String, Object>> deleteUsers(@RequestParam(defaultValue = "true") boolean keepCurrent,
                                                           Authentication authentication) {
        Long currentId = userRepository.findByEmail(authentication.getName())
                .map(User::getId)
                .orElse(null);
        long deleted = 0;
        for (User user : userRepository.findAll()) {
            if (keepCurrent && currentId != null && currentId.equals(user.getId())) {
                continue;
            }
            refreshTokenRepository.deleteByUserId(user.getId());
            userRepository.delete(user);
            deleted++;
        }
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> auditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }

    private Map<String, Object> toUserRow(User user) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", user.getId());
        row.put("fullName", user.getFullName());
        row.put("email", user.getEmail());
        row.put("role", user.getRole().name());
        row.put("blocked", user.isBlocked());
        row.put("createdAt", user.getCreatedAt());
        return row;
    }
}
