package kz.testmanagement.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kz.testmanagement.core.entity.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    private String role;

    public Role getRoleEnum() {
        if (role == null || role.isBlank()) return Role.STUDENT;
        if (role.startsWith("ROLE_")) {
            return Role.valueOf(role.substring(5));
        }
        return Role.valueOf(role);
    }
}
