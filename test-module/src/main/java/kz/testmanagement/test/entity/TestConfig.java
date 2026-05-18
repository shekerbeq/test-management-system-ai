package kz.testmanagement.test.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import kz.testmanagement.core.dto.QuestionDto;

import java.util.List;

@Entity
@Table(name = "test_configs")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TestConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String title;

    @Column(nullable = false)
    @NotBlank
    private String topic;

    @Column(nullable = false)
    @NotBlank
    private String difficulty;

    @Column(name = "question_type", nullable = false)
    @NotBlank
    private String questionType;

    @Column(nullable = false)
    @Min(1)
    @Max(30)
    private int count;

    @Column(name = "timer_min")
    @Min(1)
    @Max(240)
    private int timerMin;

    @Column(nullable = false)
    @NotBlank
    private String language;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "access_code", unique = true)
    private String accessCode;

    @Column(nullable = false)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, PUBLISHED, CLOSED, ARCHIVED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Transient
    private List<QuestionDto> previewQuestions;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (accessCode == null || accessCode.isBlank()) {
            accessCode = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}
