package kz.testmanagement.aigenerator.service;

import kz.testmanagement.aigenerator.client.DeepSeekClient;
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
    private final DeepSeekClient deepSeekClient;
    private final QuestionParser questionParser;

    public List<QuestionDto> generateQuestions(String topic, String difficulty,
                                               String questionType, String language,
                                               int questionCount) {
        String prompt = promptBuilder.build(topic, difficulty, questionType, language, questionCount);
        List<String> rawJsonQuestions = deepSeekClient.generateQuestions(prompt);
        return rawJsonQuestions.stream()
                .map(questionParser::parseSingle)
                .toList();
    }
}