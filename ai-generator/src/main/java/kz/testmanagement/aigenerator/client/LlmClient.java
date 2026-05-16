package kz.testmanagement.aigenerator.client;

import kz.testmanagement.core.dto.QuestionDto;
import java.util.List;

public interface LlmClient {
    List<QuestionDto> generateQuestions(String prompt);
}