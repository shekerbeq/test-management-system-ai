package kz.testmanagement.aigenerator.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestionParserTest {

    @Test
    void parsesJsonArray() {
        QuestionParser parser = new QuestionParser();
        var result = parser.parseQuestions("""
                [{"question":"Q","options":["A","B"],"correctIndices":[0],"correct":0,"open":false}]
                """);
        assertEquals(1, result.size());
        assertEquals("Q", result.get(0).getQuestion());
        assertFalse(result.get(0).isOpen());
    }
}
