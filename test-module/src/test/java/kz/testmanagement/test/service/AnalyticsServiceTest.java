package kz.testmanagement.test.service;

import kz.testmanagement.test.entity.TestSession;
import kz.testmanagement.test.repository.TestSessionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsServiceTest {

    @Test
    void calculatesCompletedAttempts() {
        TestSessionRepository repository = mock(TestSessionRepository.class);
        TestSession completed = TestSession.builder().status("COMPLETED").score(4).build();
        TestSession started = TestSession.builder().status("IN_PROGRESS").build();
        when(repository.findByTestConfig_Id(1L)).thenReturn(List.of(completed, started));

        var stats = new AnalyticsService(repository).getTestStatistics(1L);

        assertEquals(2L, stats.get("totalAttempts"));
        assertEquals(1L, stats.get("completedAttempts"));
    }
}
