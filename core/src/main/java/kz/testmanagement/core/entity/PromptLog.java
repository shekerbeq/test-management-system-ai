package kz.testmanagement.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String promptHash;

    private String topic;

    private String provider;

    private String model;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
