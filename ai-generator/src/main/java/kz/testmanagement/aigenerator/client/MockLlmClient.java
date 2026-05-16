package kz.testmanagement.aigenerator.client;

import kz.testmanagement.core.dto.QuestionDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "mock", matchIfMissing = true)
public class MockLlmClient implements LlmClient {
    @Override
    public List<QuestionDto> generateQuestions(String prompt) {
        return List.of(
                new QuestionDto("Что такое Spring Boot?", List.of("Фреймворк", "Библиотека", "Сервер", "Язык"), List.of(0), 0, false),
                new QuestionDto("Какие типы внедрения зависимостей существуют?",
                        List.of("Конструктор", "Сеттер", "Поле", "Все варианты"),
                        List.of(0,1,2), null, false),
                new QuestionDto("Объясните принцип работы JWT.", List.of(), List.of(), null, true)
        );
    }
}
