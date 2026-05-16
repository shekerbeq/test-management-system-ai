package kz.testmanagement.aigenerator.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final WebClient webClient;
    private final QuestionParser questionParser;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeepSeekClient(WebClient.Builder webClientBuilder,
                          @Value("${deepseek.api-key}") String apiKey,
                          @Value("${deepseek.model}") String model,
                          @Value("${deepseek.url}") String url,
                          QuestionParser questionParser) {
        this.webClient = webClientBuilder.baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.apiKey = apiKey;
        this.model = model;
        this.questionParser = questionParser;
    }

    @Override
    public List<QuestionDto> generateQuestions(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "Ты — эксперт по генерации тестов. Всегда отвечай только валидным JSON."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        String response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(15))
                .onErrorResume(e -> {
                    System.err.println("DeepSeek API ошибка: " + e.getMessage());
                    return Mono.just(""); // fallback
                })
                .block();

        if (response == null || response.isBlank()) {
            return List.of(); // пустой список вместо null
        }

        return questionParser.parseQuestions(extractAssistantContent(response));
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
