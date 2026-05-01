package kz.testmanagement.aigenerator.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.testmanagement.core.dto.QuestionDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuestionDto parseSingle(String json) {
        try {
            return objectMapper.readValue(json, QuestionDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Жеке сұрақты парсингтеу қатесі: " + e.getMessage(), e);
        }
    }
}