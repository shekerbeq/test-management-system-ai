package kz.testmanagement.aigenerator.service;

import kz.testmanagement.aigenerator.client.LlmClient;
import kz.testmanagement.aigenerator.parser.QuestionParser;
import kz.testmanagement.aigenerator.prompt.PromptBuilder;
import kz.testmanagement.core.dto.QuestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiGeneratorService {

    private final PromptBuilder promptBuilder;
    private final LlmClient llmClient;
    private final QuestionParser questionParser;

    public List<QuestionDto> generateQuestions(String topic, String difficulty,
                                               String questionType, String language,
                                               int questionCount) {
        String prompt = promptBuilder.build(topic, difficulty, questionType, language, questionCount);
        List<String> rawResponses = llmClient.generateQuestions(prompt);
        // Так как мок возвращает сразу список строк, а не JSON, пока пропустим парсинг
        // В реальной реализации LlmClient вернёт JSON-строку, которую нужно парсить.
        // Для совместимости с моком возвращаем простые объекты.
        return rawResponses.stream()
                .map(q -> new QuestionDto(q, List.of("А", "Б", "В", "Г"), 0))
                .toList();
    }
}