package kz.testmanagement.aigenerator.client;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class MockLlmClient implements LlmClient {

    @Override
    public List<String> generateQuestions(String prompt) {
        return Arrays.asList(
                "Вопрос 1: Что такое Spring Boot?",
                "Вопрос 2: Какие бывают типы внедрения зависимостей?",
                "Вопрос 3: Как работает JWT?"
        );
    }
}