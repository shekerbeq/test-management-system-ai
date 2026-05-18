package kz.testmanagement.user.security;

import kz.testmanagement.core.entity.Role;
import kz.testmanagement.core.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "qT6Nf9vY2kL8wR5dX1eS3aC7zG0bM4nO+0dF1gH2iJ3kL4mN5oP6qR7sT8uV9wX0yZ1aB2cD3eF4gH5iJ6kL7";

    @Test
    void generatesAndValidatesJwtForRoleBasedUser() {
        JwtService jwtService = new JwtService(SECRET, 900000);
        User user = new User();
        user.setId(7L);
        user.setEmail("teacher@example.com");
        user.setRole(Role.TEACHER);

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("teacher@example.com", jwtService.extractEmail(token));
    }

    @Test
    void rejectsInvalidJwt() {
        JwtService jwtService = new JwtService(SECRET, 900000);
        assertFalse(jwtService.isTokenValid("invalid-token"));
    }
}
