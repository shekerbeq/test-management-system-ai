package kz.testmanagement.aigenerator.prompt;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String build(String topic, String difficulty, String questionType,
                        String language, int questionCount) {
        String type = questionType.toLowerCase();
        String promptTemplate;

        if (type.contains("single") || type.equals("один") || type.equals("single")) {
            promptTemplate = buildSinglePrompt(topic, difficulty, language, questionCount);
        } else if (type.contains("multiple") || type.equals("несколько")) {
            promptTemplate = buildMultiplePrompt(topic, difficulty, language, questionCount);
        } else if (type.contains("open") || type.equals("открытый")) {
            promptTemplate = buildOpenPrompt(topic, difficulty, language, questionCount);
        } else {
            promptTemplate = buildMixedPrompt(topic, difficulty, language, questionCount);
        }

        return promptTemplate;
    }

    private String buildSinglePrompt(String topic, String difficulty, String language, int count) {
        return String.format(
                "Ты — генератор тестов. Создай %d вопросов с одним правильным ответом на тему \"%s\" сложностью %s. Язык: %s.\n" +
                        "Формат ответа: верни ТОЛЬКО JSON массив объектов. Каждый объект имеет поля:\n" +
                        "  \"question\": текст вопроса,\n" +
                        "  \"options\": массив из 4 строк (варианты ответа),\n" +
                        "  \"correct\": индекс правильного ответа (0-3).\n" +
                        "Пример: [{\"question\":\"Что такое Java?\",\"options\":[\"Язык программирования\",\"Остров\",\"Кофе\",\"Другое\"],\"correct\":0}]\n" +
                        "Не добавляй лишний текст, только JSON.",
                count, topic, difficulty, language
        );
    }

    private String buildMultiplePrompt(String topic, String difficulty, String language, int count) {
        return String.format(
                "Ты — генератор тестов. Создай %d вопросов с НЕСКОЛЬКИМИ правильными ответами на тему \"%s\" сложностью %s. Язык: %s.\n" +
                        "Формат ответа: верни ТОЛЬКО JSON массив объектов. Каждый объект имеет поля:\n" +
                        "  \"question\": текст вопроса,\n" +
                        "  \"options\": массив из 4 строк (варианты ответа),\n" +
                        "  \"correctIndices\": массив целых чисел (индексы правильных ответов, от 0 до 3).\n" +
                        "Пример: [{\"question\":\"Какие из следующих языков компилируемые?\",\"options\":[\"Java\",\"Python\",\"C++\",\"JavaScript\"],\"correctIndices\":[0,2]}]\n" +
                        "Не добавляй лишний текст, только JSON.",
                count, topic, difficulty, language
        );
    }

    private String buildOpenPrompt(String topic, String difficulty, String language, int count) {
        return String.format(
                "Ты — генератор тестов. Создай %d открытых вопросов (без вариантов ответа) на тему \"%s\" сложностью %s. Язык: %s.\n" +
                        "Формат ответа: верни ТОЛЬКО JSON массив объектов. Каждый объект имеет поля:\n" +
                        "  \"question\": текст вопроса,\n" +
                        "  \"open\": true.\n" +
                        "Пример: [{\"question\":\"Объясните принципы ООП\",\"open\":true}]\n" +
                        "Не добавляй лишний текст, только JSON.",
                count, topic, difficulty, language
        );
    }

    private String buildMixedPrompt(String topic, String difficulty, String language, int count) {
        return String.format(
                "Ты — генератор тестов. Создай %d вопросов смешанного типа (часть с одним правильным ответом, часть с несколькими, часть открытых) на тему \"%s\" сложностью %s. Язык: %s.\n" +
                        "Формат: JSON массив. Для вопросов с одним правильным ответом используй поля \"question\", \"options\" (4 варианта), \"correct\" (индекс).\n" +
                        "Для вопросов с несколькими правильными ответами: \"question\", \"options\", \"correctIndices\" (массив индексов).\n" +
                        "Для открытых: \"question\", \"open\": true.\n" +
                        "Не добавляй лишний текст, только JSON.",
                count, topic, difficulty, language
        );
    }
}
