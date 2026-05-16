package kz.testmanagement.aigenerator.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.testmanagement.core.dto.QuestionDto;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class QuestionParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<QuestionDto> parseQuestions(String jsonResponse) {
        try {
            String cleanJson = extractJson(jsonResponse);
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.replaceAll("```json\\s*", "").replaceAll("\\s*```$", "");
            }
            JsonNode root = objectMapper.readTree(cleanJson);
            List<QuestionDto> result = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    QuestionDto dto = parseSingleNode(node);
                    if (dto != null) result.add(dto);
                }
            } else {
                QuestionDto dto = parseSingleNode(root);
                if (dto != null) result.add(dto);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("JSON парсинг қатесі: " + e.getMessage(), e);
        }
    }

    private String extractJson(String value) {
        String clean = value == null ? "" : value.trim();
        int arrayStart = clean.indexOf('[');
        int arrayEnd = clean.lastIndexOf(']');
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            return clean.substring(arrayStart, arrayEnd + 1);
        }
        int objectStart = clean.indexOf('{');
        int objectEnd = clean.lastIndexOf('}');
        if (objectStart >= 0 && objectEnd > objectStart) {
            return clean.substring(objectStart, objectEnd + 1);
        }
        return clean;
    }

    private QuestionDto parseSingleNode(JsonNode node) {
        try {
            boolean isOpen = node.has("open") && node.get("open").asBoolean();
            String question = node.has("question") ? node.get("question").asText() : "";
            List<String> options = new ArrayList<>();
            if (node.has("options") && node.get("options").isArray()) {
                for (JsonNode opt : node.get("options")) {
                    options.add(opt.asText());
                }
            }
            Integer correct = null;
            if (node.has("correct") && !node.get("correct").isNull()) {
                correct = node.get("correct").asInt();
            }
            List<Integer> correctIndices = new ArrayList<>();
            if (node.has("correctIndices") && node.get("correctIndices").isArray()) {
                for (JsonNode idx : node.get("correctIndices")) {
                    correctIndices.add(idx.asInt());
                }
            }
            if (correctIndices.isEmpty() && correct != null) {
                correctIndices.add(correct);
            }
            QuestionDto dto = new QuestionDto(question, options, correctIndices, correct, isOpen);
            return dto;
        } catch (Exception e) {
            System.err.println("Парсинг одного вопроса не удался: " + e.getMessage());
            return null;
        }
    }
}
