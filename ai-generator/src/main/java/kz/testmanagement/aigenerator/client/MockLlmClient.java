package kz.testmanagement.aigenerator.client;

import kz.testmanagement.core.dto.QuestionDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "mock", matchIfMissing = true)
public class MockLlmClient implements LlmClient {

    private static final Pattern TOPIC_PATTERN = Pattern.compile("Тақырып:\\s*\"([^\"]+)\"");
    private static final Pattern COUNT_PATTERN = Pattern.compile("(\\d+)\\s+сұрақ");

    @Override
    public List<QuestionDto> generateQuestions(String prompt) {
        String topic = extractTopic(prompt);
        int count = extractCount(prompt);
        return IntStream.range(0, count)
                .mapToObj(index -> buildQuestion(topic, index))
                .toList();
    }

    private QuestionDto buildQuestion(String topic, int index) {
        return switch (index % 6) {
            case 0 -> single(
                    topic + " не үшін қолданылады?",
                    "Нақты мәселені шешу үшін",
                    "Тек атауын жаттау үшін",
                    "Тақырыптан тыс мысал үшін",
                    "Кездейсоқ жауап үшін"
            );
            case 1 -> single(
                    topic + " бойынша жиі кездесетін қате қайсы?",
                    "Негізгі шартты ескермеу",
                    "Нәтижені тексеру",
                    "Мысал келтіру",
                    "Ұғымдарды салыстыру"
            );
            case 2 -> multiple(
                    topic + " жақсы түсінілгенін көрсететін белгілерді таңдаңыз.",
                    List.of("Түсіндіре алады", "Мысал келтіреді", "Тақырыптан ауытқиды", "Шектеулерін біледі"),
                    List.of(0, 1, 3)
            );
            case 3 -> single(
                    topic + " үшін ең дәл тұжырым қайсы?",
                    "Мақсатқа сай қолданылатын ұғым немесе тәсіл",
                    "Кез келген тақырыпқа ортақ жауап",
                    "Тек жаттауға арналған сөз",
                    "Практикада қолданылмайды"
            );
            case 4 -> open(topic + " бойынша қысқа мысал келтіріңіз.");
            default -> multiple(
                    topic + " туралы сапалы жауапқа не кіреді?",
                    List.of("Нақты контекст", "Айқын негізгі ой", "Тақырыптан алыс мысал", "Қысқа түсіндіру"),
                    List.of(0, 1, 3)
            );
        };
    }

    private QuestionDto single(String question, String correct, String second, String third, String fourth) {
        return new QuestionDto(question, List.of(correct, second, third, fourth), List.of(0), 0, false);
    }

    private QuestionDto multiple(String question, List<String> options, List<Integer> correctIndices) {
        return new QuestionDto(question, options, correctIndices, correctIndices.get(0), false);
    }

    private QuestionDto open(String question) {
        return new QuestionDto(question, List.of(), List.of(), null, true);
    }

    private String extractTopic(String prompt) {
        Matcher matcher = TOPIC_PATTERN.matcher(prompt == null ? "" : prompt);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "берілген тақырып";
    }

    private int extractCount(String prompt) {
        Matcher matcher = COUNT_PATTERN.matcher(prompt == null ? "" : prompt);
        if (matcher.find()) {
            return Math.max(1, Integer.parseInt(matcher.group(1)));
        }
        return 5;
    }
}
