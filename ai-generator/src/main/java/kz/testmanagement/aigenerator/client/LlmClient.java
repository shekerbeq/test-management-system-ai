package kz.testmanagement.aigenerator.client;

import java.util.List;

public interface LlmClient {
    List<String> generateQuestions(String prompt);
}