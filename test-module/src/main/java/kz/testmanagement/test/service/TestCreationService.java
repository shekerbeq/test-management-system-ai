package kz.testmanagement.test.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.testmanagement.aigenerator.service.AiGeneratorService;
import kz.testmanagement.core.dto.QuestionDto;
import kz.testmanagement.test.entity.Question;
import kz.testmanagement.test.entity.TestConfig;
import kz.testmanagement.test.repository.QuestionRepository;
import kz.testmanagement.test.repository.TestConfigRepository;
import kz.testmanagement.test.repository.UserAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCreationService {

    private final TestConfigRepository testConfigRepository;
    private final QuestionRepository questionRepository;
    private final UserAnswerRepository answerRepository;
    private final AiGeneratorService aiGeneratorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public TestConfig createTestWithQuestions(TestConfig config) {
        // 1. Сохраняем конфиг теста
        TestConfig saved = testConfigRepository.save(config);

        List<QuestionDto> dtos = config.getPreviewQuestions() != null && !config.getPreviewQuestions().isEmpty()
                ? config.getPreviewQuestions()
                : aiGeneratorService.generateQuestions(
                        saved.getTopic(),
                        saved.getDifficulty(),
                        saved.getQuestionType(),
                        saved.getLanguage(),
                        saved.getCount()
                );

        // 3. Превращаем DTO в сущности Question и сохраняем
        for (QuestionDto dto : dtos) {
            String optionsJson;
            try {
                optionsJson = objectMapper.writeValueAsString(dto.getOptions());
            } catch (JsonProcessingException e) {
                optionsJson = "[]";
            }
            Question q = Question.builder()
                    .testConfig(saved)
                    .text(dto.getQuestion())
                    .optionsJson(optionsJson)
                    .correctIndex(resolveLegacyCorrectIndex(dto))
                    .correctIndicesJson(writeList(dto.getCorrectIndices()))
                    .openQuestion(dto.isOpen())
                    .build();
            questionRepository.save(q);
        }

        return saved;
    }

    @Transactional
    public TestConfig replaceQuestions(TestConfig config, List<QuestionDto> dtos) {
        List<Question> existing = questionRepository.findByTestConfig_Id(config.getId());
        for (Question question : existing) {
            answerRepository.deleteByQuestion_Id(question.getId());
        }
        questionRepository.deleteAll(existing);
        saveQuestions(config, dtos == null ? List.of() : dtos);
        return testConfigRepository.save(config);
    }

    @Transactional
    public TestConfig copyTest(TestConfig source, Long creatorId) {
        TestConfig copy = TestConfig.builder()
                .title(source.getTitle() + " (копия)")
                .topic(source.getTopic())
                .difficulty(source.getDifficulty())
                .questionType(source.getQuestionType())
                .count(source.getCount())
                .timerMin(source.getTimerMin())
                .language(source.getLanguage())
                .creatorId(creatorId)
                .status("DRAFT")
                .build();
        TestConfig saved = testConfigRepository.save(copy);
        List<QuestionDto> questions = questionRepository.findByTestConfig_Id(source.getId()).stream()
                .map(question -> new QuestionDto(
                        question.getId(),
                        question.getText(),
                        readOptions(question.getOptionsJson()),
                        readIndices(question.getCorrectIndicesJson()),
                        question.getCorrectIndex(),
                        question.isOpenQuestion()
                ))
                .toList();
        saveQuestions(saved, questions);
        saved.setCount(questions.size());
        return testConfigRepository.save(saved);
    }

    private void saveQuestions(TestConfig saved, List<QuestionDto> dtos) {
        for (QuestionDto dto : dtos) {
            String optionsJson;
            try {
                optionsJson = objectMapper.writeValueAsString(dto.getOptions());
            } catch (JsonProcessingException e) {
                optionsJson = "[]";
            }
            Question q = Question.builder()
                    .testConfig(saved)
                    .text(dto.getQuestion())
                    .optionsJson(optionsJson)
                    .correctIndex(resolveLegacyCorrectIndex(dto))
                    .correctIndicesJson(writeList(dto.getCorrectIndices()))
                    .openQuestion(dto.isOpen())
                    .build();
            questionRepository.save(q);
        }
    }

    private String writeList(List<Integer> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<Integer> readIndices(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<String> readOptions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private Integer resolveLegacyCorrectIndex(QuestionDto dto) {
        if (dto.getCorrect() != null) {
            return dto.getCorrect();
        }
        if (dto.getCorrectIndices() != null && !dto.getCorrectIndices().isEmpty()) {
            return dto.getCorrectIndices().get(0);
        }
        return 0;
    }
}
