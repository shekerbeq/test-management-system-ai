package kz.testmanagement.user.dto;

import kz.testmanagement.core.entity.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;       // фронтенд шлёт "name"
    private String email;
    private String password;
    private String role;       // "ROLE_STUDENT" или "ROLE_TEACHER"

    public Role getRoleEnum() {
        if (role == null) return Role.STUDENT;
        if (role.startsWith("ROLE_")) {
            return Role.valueOf(role.substring(5));   // убираем "ROLE_"
        }
        return Role.valueOf(role);
    }
}