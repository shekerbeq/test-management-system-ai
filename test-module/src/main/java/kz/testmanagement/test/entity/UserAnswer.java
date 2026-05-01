package kz.testmanagement.test.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_answers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private TestSession testSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "selected_index")
    private Integer selectedIndex;   // индекс выбранного варианта (для закрытых вопросов)

    @Column(name = "open_answer", columnDefinition = "TEXT")
    private String openAnswer;       // текстовый ответ для открытых вопросов

    @Column(name = "is_correct")
    private Boolean isCorrect;       // заполняется после проверки
}