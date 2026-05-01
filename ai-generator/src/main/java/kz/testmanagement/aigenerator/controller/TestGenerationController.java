package kz.testmanagement.aigenerator.controller;

import kz.testmanagement.aigenerator.service.AiGeneratorService;
import kz.testmanagement.core.dto.QuestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestGenerationController {

    private final AiGeneratorService aiGeneratorService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<QuestionDto>> generateTest(
            @RequestParam String topic,
            @RequestParam(defaultValue = "Medium") String difficulty,
            @RequestParam(defaultValue = "Multiple") String questionType,
            @RequestParam(defaultValue = "ru") String language,
            @RequestParam(defaultValue = "5") int count
    ) {
        List<QuestionDto> questions = aiGeneratorService.generateQuestions(topic, difficulty, questionType, language, count);
        return ResponseEntity.ok(questions);
    }
}