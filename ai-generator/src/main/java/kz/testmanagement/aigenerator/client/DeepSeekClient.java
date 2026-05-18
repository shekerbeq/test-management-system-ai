package kz.testmanagement.aigenerator.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import kz.testmanagement.aigenerator.parser.QuestionParser;
import kz.testmanagement.core.dto.QuestionDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "deepseek")
public class DeepSeekClient implements LlmClient {

    private static final int FAILURE_THRESHOLD = 3;
    private static final long OPEN_MILLIS = 30_000;

    private final WebClient webClient;
    private final QuestionParser questionParser;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private int failures = 0;
    private long circuitOpenedAt = 0;

    public DeepSeekClient(WebClient.Builder webClientBuilder,
                          @Value("${deepseek.api-key}") String apiKey,
                          @Value("${deepseek.model}") String model,
                          @Value("${deepseek.url}") String url,
                          QuestionParser questionParser) {
        this.webClient = webClientBuilder.baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
        this.questionParser = questionParser;
    }

    @Override
    @Retry(name = "llm")
    @CircuitBreaker(name = "llm", fallbackMethod = "fallback")
    public List<QuestionDto> generateQuestions(String prompt) {
        if (isCircuitOpen()) {
            return List.of();
        }
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "Return only valid JSON for test questions."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        String response = "";
        for (int attempt = 1; attempt <= 3; attempt++) {
            response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorResume(e -> Mono.just(""))
                    .block();
            if (response != null && !response.isBlank()) {
                failures = 0;
                break;
            }
        }

        if (response == null || response.isBlank()) {
            failures++;
            if (failures >= FAILURE_THRESHOLD) {
                circuitOpenedAt = System.currentTimeMillis();
            }
            return List.of();
        }

        return questionParser.parseQuestions(extractAssistantContent(response));
    }

    public List<QuestionDto> fallback(String prompt, Throwable throwable) {
        return List.of();
    }

    private boolean isCircuitOpen() {
        if (circuitOpenedAt == 0) {
            return false;
        }
        if (System.currentTimeMillis() - circuitOpenedAt > OPEN_MILLIS) {
            circuitOpenedAt = 0;
            failures = 0;
            return false;
        }
        return true;
    }

    private String extractAssistantContent(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText(response);
            }
        } catch (Exception ignored) {
            return response;
        }
        return response;
    }
}
