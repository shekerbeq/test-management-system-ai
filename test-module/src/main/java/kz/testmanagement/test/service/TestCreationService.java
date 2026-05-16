package kz.testmanagement.test.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.testmanagement.aigenerator.service.AiGeneratorService;
import kz.testmanagement.core.dto.QuestionDto;
import kz.testmanagement.test.entity.Question;
import kz.testmanagement.test.entity.TestConfig;
import kz.testmanagement.test.repository.QuestionRepository;
import kz.testmanagement.test.repository.TestConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCreationService {

    private final TestConfigRepository testConfigRepository;
    private final QuestionRepository questionRepository;
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
                    .correctIndex(dto.getCorrect())
                    .correctIndicesJson(writeList(dto.getCorrectIndices()))
                    .openQuestion(dto.isOpen())
                    .build();
            questionRepository.save(q);
        }

        return saved;
    }

    private String writeList(List<Integer> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
