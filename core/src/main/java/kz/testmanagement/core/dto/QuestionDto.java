package kz.testmanagement.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private String question;
    private List<String> options;
    private List<Integer> correctIndices;
    private Integer correct;
    private boolean open;

    public QuestionDto(String question, List<String> options, Integer correct) {
        this.question = question;
        this.options = options;
        this.correct = correct;
        this.correctIndices = correct == null ? List.of() : List.of(correct);
        this.open = false;
    }

    public static QuestionDto openQuestion(String question) {
        return new QuestionDto(question, List.of(), List.of(), null, true);
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
