// AuthController.java (полная замена)
package kz.testmanagement.user.controller;

import kz.testmanagement.core.entity.Role;
import kz.testmanagement.core.entity.User;
import kz.testmanagement.user.dto.LoginRequest;
import kz.testmanagement.user.dto.RegisterRequest;
import kz.testmanagement.user.security.JwtService;
import kz.testmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setFullName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRoleEnum() == null ? Role.STUDENT : request.getRoleEnum());
        userService.registerUser(user);
        return ResponseEntity.ok("Тіркелу сәтті аяқталды");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        var userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Қате email немесе құпия сөз"));
        }
        String token = jwtService.generateToken(request.getEmail());
        User user = userOpt.get();
        Map<String, Object> response = Map.of(
                "token", token,
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name(),     // STUDENT, TEACHER, ADMIN
                "fullName", user.getFullName()
        );
        return ResponseEntity.ok(response);
    }
}
