package kz.testmanagement.aigenerator.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DeepSeekClient implements LlmClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeepSeekClient(WebClient.Builder webClientBuilder,
                          @Value("${deepseek.api-key}") String apiKey,
                          @Value("${deepseek.model}") String model,
                          @Value("${deepseek.url}") String url) {
        this.webClient = webClientBuilder.baseUrl(url).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public List<String> generateQuestions(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "Ты помогаешь генерировать тестовые вопросы."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        String response = webClient.post()
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    System.err.println("DeepSeek API қатесі: " + e.getMessage());
                    return Mono.just("");
                })
                .block();

        if (response == null || response.isEmpty()) {
            return List.of();   // fallback – пустой список
        }

        return extractQuestionsFromResponse(response);
    }

    private List<String> extractQuestionsFromResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText();
                // Ожидаем JSON-массив в content, парсим его
                JsonNode questions = objectMapper.readTree(content);
                List<String> result = new ArrayList<>();
                if (questions.isArray()) {
                    for (JsonNode q : questions) {
                        result.add(objectMapper.writeValueAsString(q));
                    }
                }
                return result;
            }
        } catch (Exception e) {
            System.err.println("Жауапты өңдеу қатесі: " + e.getMessage());
        }
        return List.of();
    }
}