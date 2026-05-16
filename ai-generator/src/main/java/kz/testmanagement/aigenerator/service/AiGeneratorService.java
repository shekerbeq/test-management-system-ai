package kz.testmanagement.aigenerator.service;

import kz.testmanagement.aigenerator.client.LlmClient;
import kz.testmanagement.aigenerator.prompt.PromptBuilder;
import kz.testmanagement.core.dto.QuestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiGeneratorService {

    private final PromptBuilder promptBuilder;
    private final LlmClient llmClient;  // теперь интерфейс, можно подставить DeepSeekClient или Mock

    public List<QuestionDto> generateQuestions(String topic, String difficulty,
                                               String questionType, String language,
                                               int questionCount) {
        String prompt = promptBuilder.build(topic, difficulty, questionType, language, questionCount);
        return llmClient.generateQuestions(prompt);
    }
}