package kz.testmanagement.test.dto;

import java.util.List;

public record AnswerReviewDto(
        Long answerId,
        Long questionId,
        String question,
        List<String> options,
        List<Integer> selectedIndices,
        List<Integer> correctIndices,
        String openAnswer,
        Boolean correct,
        boolean open
) {
}
