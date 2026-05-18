package kz.testmanagement.user.controller;

import kz.testmanagement.core.dto.AuthResponse;
import kz.testmanagement.core.entity.Role;
import kz.testmanagement.core.entity.User;
import kz.testmanagement.user.dto.LoginRequest;
import kz.testmanagement.user.dto.RegisterRequest;
import kz.testmanagement.user.security.JwtService;
import kz.testmanagement.user.service.LoginAttemptService;
import kz.testmanagement.user.service.RefreshTokenService;
import kz.testmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setFullName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRoleEnum() == null ? Role.STUDENT : request.getRoleEnum());
        User saved = userService.registerUser(user);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String login = resolveLogin(request.getEmail());
        if (loginAttemptService.isBlocked(login)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Кіру әрекеті уақытша бұғатталды");
        }
        var userOpt = userService.findByEmail(login);
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            loginAttemptService.loginFailed(login);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email немесе құпия сөз дұрыс емес");
        }
        if (userOpt.get().isBlocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Аккаунт заблокирован администратором");
        }
        loginAttemptService.loginSucceeded(login);
        return ResponseEntity.ok(toResponse(userOpt.get()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        var refreshToken = refreshTokenService.verify(request.get("refreshToken"));
        if (refreshToken.getUser().isBlocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Аккаунт заблокирован администратором");
        }
        return ResponseEntity.ok(toResponse(refreshToken.getUser()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> request) {
        refreshTokenService.revoke(request.get("refreshToken"));
        return ResponseEntity.noContent().build();
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                refreshTokenService.create(user).getToken(),
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullName()
        );
    }

    private String resolveLogin(String value) {
        if (value != null && "admin".equalsIgnoreCase(value.trim())) {
            return "admin@local";
        }
        return value == null ? "" : value.trim();
    }
}
