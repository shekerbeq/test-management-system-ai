package kz.testmanagement.aigenerator.prompt;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String build(String topic, String difficulty, String questionType,
                        String language, int questionCount) {
        return String.format(
                "Создай %d %s вопросов на тему \"%s\" сложностью %s. Язык: %s. Верни ответ в формате JSON: [{\"question\": \"...\", \"options\": [\"A\",\"B\",\"C\",\"D\"], \"correct\": 0}]",
                questionCount, questionType, topic, difficulty, language
        );
    }
}