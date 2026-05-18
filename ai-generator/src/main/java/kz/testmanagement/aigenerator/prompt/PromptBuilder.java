package kz.testmanagement.aigenerator.prompt;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String build(String topic, String difficulty, String questionType,
                        String language, int questionCount) {
        String normalizedType = questionType == null ? "Mixed" : questionType.toLowerCase();
        String normalizedLanguage = normalizeLanguage(language);
        String safeTopic = sanitize(topic);
        String safeDifficulty = sanitize(difficulty);

        if (normalizedType.contains("single")) {
            return buildSinglePrompt(safeTopic, safeDifficulty, normalizedLanguage, questionCount);
        }
        if (normalizedType.contains("multiple")) {
            return buildMultiplePrompt(safeTopic, safeDifficulty, normalizedLanguage, questionCount);
        }
        if (normalizedType.contains("open")) {
            return buildOpenPrompt(safeTopic, safeDifficulty, normalizedLanguage, questionCount);
        }
        return buildMixedPrompt(safeTopic, safeDifficulty, normalizedLanguage, questionCount);
    }

    private String commonRules(String topic, String difficulty, String language, int count) {
        return String.format("""
                Сен тест сұрақтарын құрастыратын мамансың.
                Міндет: дәл осы тақырып бойынша %d сұрақ жаса.
                Тақырып: "%s".
                Күрделілік: %s.
                Жауап тілі: %s.

                Маңызды талаптар:
                - Сұрақтар тек берілген тақырыпқа қатысты болсын. Басқа пәнге, басқа тақырыпқа, жалпы білімге ауыспа.
                - Тест атауын емес, "Тақырып" мәнін негізгі контекст ретінде қолдан.
                - Егер тақырып тар болса, сұрақтарды сол тар контекст ішінде құрастыр.
                - Барлық сұрақ, жауап нұсқалары және түсініктеме осы тілде болсын: %s.
                - JSON-нан басқа мәтін жазба.
                - Markdown, ```json, кіріспе, қорытынды қоспа.
                - Дәл %d объектіден тұратын JSON массив қайтар.
                - Сұрақтар бір-бірін қайталамасын және "негізгі ұғымы қайсы" сияқты бір шаблонға байланып қалмасын.
                - Сұрақтарды әртүрлі формада құрастыр: анықтама, практикалық жағдай, қате табу, салыстыру, себеп-салдар, қысқа кейс, қолдану мысалы.
                - Жауап нұсқалары да мағыналы болсын: тым күлкілі, тым оңай немесе тақырыптан мүлдем алыс нұсқаларды көп қолданба.
                - Сұрақ мәтініне реттік нөмір жазба.
                """, count, topic, difficulty, language, language, count);
    }

    private String buildSinglePrompt(String topic, String difficulty, String language, int count) {
        return commonRules(topic, difficulty, language, count) + """

                Сұрақ түрі: бір дұрыс жауап.
                Әр объект форматы:
                {
                  "question": "сұрақ мәтіні",
                  "options": ["A", "B", "C", "D"],
                  "correct": 0,
                  "correctIndices": [0],
                  "open": false
                }
                "options" міндетті түрде 4 нұсқадан тұрсын. "correct" 0 мен 3 аралығында болсын.
                """;
    }

    private String buildMultiplePrompt(String topic, String difficulty, String language, int count) {
        return commonRules(topic, difficulty, language, count) + """

                Сұрақ түрі: бірнеше дұрыс жауап.
                Әр объект форматы:
                {
                  "question": "сұрақ мәтіні",
                  "options": ["A", "B", "C", "D"],
                  "correct": 0,
                  "correctIndices": [0, 2],
                  "open": false
                }
                "options" міндетті түрде 4 нұсқадан тұрсын. "correctIndices" кемінде 2 индекс қамтысын.
                """;
    }

    private String buildOpenPrompt(String topic, String difficulty, String language, int count) {
        return commonRules(topic, difficulty, language, count) + """

                Сұрақ түрі: ашық сұрақ.
                Әр объект форматы:
                {
                  "question": "ашық сұрақ мәтіні",
                  "options": [],
                  "correct": 0,
                  "correctIndices": [],
                  "open": true
                }
                Ашық сұрақтарға жауап нұсқаларын қоспа.
                """;
    }

    private String buildMixedPrompt(String topic, String difficulty, String language, int count) {
        return commonRules(topic, difficulty, language, count) + """

                Сұрақ түрі: аралас.
                Массив ішінде бір жауапты, көп жауапты және ашық сұрақтар болсын.
                Бір жауапты сұрақ форматы:
                {"question":"...","options":["A","B","C","D"],"correct":0,"correctIndices":[0],"open":false}
                Көп жауапты сұрақ форматы:
                {"question":"...","options":["A","B","C","D"],"correct":0,"correctIndices":[0,2],"open":false}
                Ашық сұрақ форматы:
                {"question":"...","options":[],"correct":0,"correctIndices":[],"open":true}
                """;
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank() || "kk".equalsIgnoreCase(language)) {
            return "қазақша";
        }
        if ("ru".equalsIgnoreCase(language)) {
            return "орысша";
        }
        if ("en".equalsIgnoreCase(language)) {
            return "ағылшынша";
        }
        return sanitize(language);
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "жалпы тақырып";
        }
        String normalized = value
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replace("```", " ")
                .replace("{", " ")
                .replace("}", " ")
                .replace("[", " ")
                .replace("]", " ")
                .trim();
        normalized = normalized.replaceAll("(?i)(ignore|system|assistant|developer|instruction|prompt)", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        if (normalized.length() > 180) {
            normalized = normalized.substring(0, 180).trim();
        }
        return normalized.isBlank() ? "Р¶Р°Р»РїС‹ С‚Р°Т›С‹СЂС‹Рї" : normalized;
    }
}
