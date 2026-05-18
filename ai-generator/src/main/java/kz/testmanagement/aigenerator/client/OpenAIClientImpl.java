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

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "openai")
public class OpenAIClientImpl implements LlmClient {

    private final WebClient webClient;
    private final QuestionParser questionParser;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAIClientImpl(WebClient.Builder webClientBuilder,
                            @Value("${openai.api-key:${OPENAI_API_KEY:}}") String apiKey,
                            @Value("${openai.model:gpt-4o-mini}") String model,
                            @Value("${openai.url:https://api.openai.com/v1/chat/completions}") String url,
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
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "Return only valid JSON for test questions."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );
        String response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
        return questionParser.parseQuestions(extractAssistantContent(response));
    }

    public List<QuestionDto> fallback(String prompt, Throwable throwable) {
        return List.of();
    }

    private String extractAssistantContent(String response) {
        try {
            JsonNode choices = objectMapper.readTree(response).path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText(response);
            }
        } catch (Exception ignored) {
            return response;
        }
        return response;
    }
}
