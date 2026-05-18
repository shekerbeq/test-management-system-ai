package kz.testmanagement.user.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_SECONDS = 15 * 60;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String email) {
        Attempt attempt = attempts.get(normalize(email));
        return attempt != null && attempt.count >= MAX_ATTEMPTS && attempt.blockedUntil > Instant.now().getEpochSecond();
    }

    public void loginSucceeded(String email) {
        attempts.remove(normalize(email));
    }

    public void loginFailed(String email) {
        attempts.compute(normalize(email), (key, old) -> {
            Attempt attempt = old == null ? new Attempt() : old;
            attempt.count++;
            if (attempt.count >= MAX_ATTEMPTS) {
                attempt.blockedUntil = Instant.now().getEpochSecond() + BLOCK_SECONDS;
            }
            return attempt;
        });
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static class Attempt {
        int count;
        long blockedUntil;
    }
}
