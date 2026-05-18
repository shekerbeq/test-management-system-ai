package kz.testmanagement.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class QuestionDto {
    private Long id;
    private String question;
    private List<String> options;
    private List<Integer> correctIndices;
    private Integer correct;
    private boolean open;

    public QuestionDto(String question, List<String> options, Integer correct) {
        this(null, question, options, correct == null ? List.of() : List.of(correct), correct, false);
    }

    public QuestionDto(String question, List<String> options, List<Integer> correctIndices, Integer correct, boolean open) {
        this(null, question, options, correctIndices, correct, open);
    }

    public QuestionDto(Long id, String question, List<String> options, List<Integer> correctIndices, Integer correct, boolean open) {
        this.id = id;
        this.question = question;
        this.options = options;
        this.correctIndices = correctIndices;
        this.correct = correct;
        this.open = open;
    }

    public static QuestionDto openQuestion(String question) {
        return new QuestionDto(null, question, List.of(), List.of(), null, true);
    }

    public boolean isSingle() {
        return !open && (correct != null || (correctIndices != null && correctIndices.size() == 1));
    }
    public boolean isMultiple() {
        return !open && correctIndices != null && correctIndices.size() > 1;
    }
    public boolean isOpen() {
        return open;
    }
}
