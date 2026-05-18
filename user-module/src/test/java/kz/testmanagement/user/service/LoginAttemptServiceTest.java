package kz.testmanagement.user.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    @Test
    void blocksAfterFiveFailedAttempts() {
        LoginAttemptService service = new LoginAttemptService();
        for (int i = 0; i < 5; i++) {
            service.loginFailed("a@b.kz");
        }
        assertTrue(service.isBlocked("a@b.kz"));
        service.loginSucceeded("a@b.kz");
        assertFalse(service.isBlocked("a@b.kz"));
    }
}
