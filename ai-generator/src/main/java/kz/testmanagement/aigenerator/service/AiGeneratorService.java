package kz.testmanagement.aigenerator.service;

import kz.testmanagement.aigenerator.client.LlmClient;
import kz.testmanagement.aigenerator.prompt.PromptBuilder;
import kz.testmanagement.core.dto.QuestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiGeneratorService {

    private final PromptBuilder promptBuilder;
    private final LlmClient llmClient;
    private final QuestionCacheService questionCacheService;
    private final CostTracker costTracker;
    private final PromptLogRetentionService promptLogRetentionService;

    public List<QuestionDto> generateQuestions(String topic, String difficulty,
                                               String questionType, String language,
                                               int questionCount) {
        String prompt = promptBuilder.build(topic, difficulty, questionType, language, questionCount);
        promptLogRetentionService.record(topic, prompt);
        List<QuestionDto> generated = questionCacheService.find(prompt)
                .orElseGet(() -> {
                    List<QuestionDto> fresh = llmClient.generateQuestions(prompt);
                    questionCacheService.save(prompt, fresh);
                    return fresh;
                });
        costTracker.record(topic, prompt, generated);
        return normalizeCount(generated, topic, questionType, questionCount);
    }

    private List<QuestionDto> normalizeCount(List<QuestionDto> questions, String topic, String questionType, int expectedCount) {
        int safeCount = Math.max(1, expectedCount);
        List<QuestionDto> normalized = new ArrayList<>(questions == null ? List.of() : questions);
        if (normalized.size() > safeCount) {
            return normalized.subList(0, safeCount);
        }
        while (normalized.size() < safeCount) {
            normalized.add(buildFallbackQuestion(topic, questionType, normalized.size() + 1));
        }
        return normalized;
    }

    private QuestionDto buildFallbackQuestion(String topic, String questionType, int number) {
        String safeTopic = topic == null || topic.isBlank() ? "берілген тақырып" : topic;
        boolean open = questionType != null && questionType.equalsIgnoreCase("Open");
        if (open) {
            return new QuestionDto(safeTopic + " бойынша қысқа түсіндірме жазыңыз.", List.of(), List.of(), null, true);
        }
        return new QuestionDto(
                safeTopic + " бойынша дұрыс жауапты таңдаңыз.",
                List.of("Дұрыс тұжырым", "Қате тұжырым", "Тақырыптан тыс жауап", "Кездейсоқ жауап"),
                List.of(0),
                0,
                false
        );
    }
}
