package kz.testmanagement.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_config_id")
    private TestConfig testConfig;

    private String text;

    @Column(columnDefinition = "TEXT")
    private String optionsJson;   // храним варианты ответов как JSON строку

    private Integer correctIndex;

    @Column(columnDefinition = "TEXT")
    private String correctIndicesJson;

    @Column(nullable = false)
    @Builder.Default
    private boolean openQuestion = false;
}
