package kz.testmanagement.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private Long id;
    private String email;
    private String role;
    private String fullName;
}
