package kz.testmanagement.aigenerator.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.testmanagement.core.dto.QuestionDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<QuestionDto> parse(String jsonResponse) {
        try {
            return objectMapper.readValue(jsonResponse, new TypeReference<List<QuestionDto>>() {});
        } catch (Exception e) {
            throw new RuntimeException("JSON жауапты парсингтеу қатесі: " + e.getMessage(), e);
        }
    }
}