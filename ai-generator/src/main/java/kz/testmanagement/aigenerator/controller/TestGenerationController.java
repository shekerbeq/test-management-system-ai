package kz.testmanagement.aigenerator.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kz.testmanagement.aigenerator.service.AiGeneratorService;
import kz.testmanagement.core.dto.QuestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@Validated
public class TestGenerationController {

    private final AiGeneratorService aiGeneratorService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<QuestionDto>> generateTest(
            @RequestParam @NotBlank @Size(max = 180) String topic,
            @RequestParam(defaultValue = "Medium") @Pattern(regexp = "Easy|Medium|Hard", flags = Pattern.Flag.CASE_INSENSITIVE) String difficulty,
            @RequestParam(defaultValue = "Multiple") @Pattern(regexp = "Single|Multiple|Mixed|Open", flags = Pattern.Flag.CASE_INSENSITIVE) String questionType,
            @RequestParam(defaultValue = "ru") @Pattern(regexp = "kk|ru|en", flags = Pattern.Flag.CASE_INSENSITIVE) String language,
            @RequestParam(defaultValue = "5") @Min(1) @Max(30) int count
    ) {
        List<QuestionDto> questions = aiGeneratorService.generateQuestions(topic, difficulty, questionType, language, count);
        return ResponseEntity.ok(questions);
    }
}
