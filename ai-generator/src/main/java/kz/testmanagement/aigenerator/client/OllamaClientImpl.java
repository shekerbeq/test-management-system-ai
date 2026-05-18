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
@ConditionalOnProperty(name = "llm.provider", havingValue = "ollama")
public class OllamaClientImpl implements LlmClient {

    private final WebClient webClient;
    private final QuestionParser questionParser;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OllamaClientImpl(WebClient.Builder webClientBuilder,
                            @Value("${ollama.url:http://localhost:11434/v1/chat/completions}") String url,
                            @Value("${ollama.model:llama3}") String model,
                            QuestionParser questionParser) {
        this.webClient = webClientBuilder.baseUrl(url).build();
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
                "stream", false
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
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText(response);
            }
            return root.path("message").path("content").asText(response);
        } catch (Exception ignored) {
            return response;
        }
    }
}
