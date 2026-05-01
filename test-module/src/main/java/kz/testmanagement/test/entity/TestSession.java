package kz.testmanagement.test.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_sessions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TestSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_config_id", nullable = false)
    private TestConfig testConfig;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false)
    @Builder.Default
    private String status = "IN_PROGRESS";   // IN_PROGRESS, COMPLETED

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    private Integer score;   // баллы, заполним при завершении

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
}