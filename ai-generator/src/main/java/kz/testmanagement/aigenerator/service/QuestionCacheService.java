package kz.testmanagement.aigenerator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.testmanagement.core.dto.QuestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionCacheService {

    private static final Duration TTL = Duration.ofHours(6);

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final ObjectMapper objectMapper;

    public Optional<List<QuestionDto>> find(String prompt) {
        try {
            StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
            if (redis == null) {
                return Optional.empty();
            }
            String json = redis.opsForValue().get(key(prompt));
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, new TypeReference<>() {}));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public void save(String prompt, List<QuestionDto> questions) {
        try {
            StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
            if (redis != null && questions != null && !questions.isEmpty()) {
                redis.opsForValue().set(key(prompt), objectMapper.writeValueAsString(questions), TTL);
            }
        } catch (Exception ignored) {
            // Redis cache must never break AI generation.
        }
    }

    private String key(String prompt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((prompt == null ? "" : prompt).getBytes(StandardCharsets.UTF_8));
        return "llm:questions:" + HexFormat.of().formatHex(hash);
    }
}
