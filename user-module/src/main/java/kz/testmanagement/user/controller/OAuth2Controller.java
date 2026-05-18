package kz.testmanagement.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import kz.testmanagement.core.entity.Role;
import kz.testmanagement.core.entity.User;
import kz.testmanagement.user.security.JwtService;
import kz.testmanagement.user.service.RefreshTokenService;
import kz.testmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class OAuth2Controller {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/oauth2/success")
    public void success(@AuthenticationPrincipal OAuth2User principal, HttpServletResponse response) throws IOException {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        User user = userService.findByEmail(email).orElseGet(() -> {
            User created = new User();
            created.setEmail(email);
            created.setFullName(name == null ? email : name);
            created.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            created.setRole(Role.STUDENT);
            return userService.save(created);
        });
        String token = jwtService.generateToken(user);
        String refresh = refreshTokenService.create(user).getToken();
        response.sendRedirect("/oauth2-callback.html?token=" + token + "&refreshToken=" + refresh
                + "&id=" + user.getId() + "&email=" + user.getEmail() + "&role=" + user.getRole().name()
                + "&fullName=" + java.net.URLEncoder.encode(user.getFullName(), java.nio.charset.StandardCharsets.UTF_8));
    }
}
