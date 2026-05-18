package kz.testmanagement.user.service;

import kz.testmanagement.core.entity.RefreshToken;
import kz.testmanagement.core.entity.User;
import kz.testmanagement.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-expiration-days:7}")
    private long refreshExpirationDays;

    @Transactional
    public RefreshToken create(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes))
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(refreshExpirationDays))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verify(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token жарамсыз"));
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new IllegalArgumentException("Refresh token мерзімі өтті");
        }
        return refreshToken;
    }

    @Transactional
    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            refreshTokenRepository.deleteByToken(token);
        }
    }
}
