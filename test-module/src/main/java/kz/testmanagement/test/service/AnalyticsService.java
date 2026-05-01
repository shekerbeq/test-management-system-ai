package kz.testmanagement.test.service;

import kz.testmanagement.test.entity.TestSession;
import kz.testmanagement.test.repository.TestSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TestSessionRepository sessionRepository;

    public Map<String, Object> getTestStatistics(Long testConfigId) {
        List<TestSession> sessions = sessionRepository.findByTestConfigId(testConfigId);
        long totalAttempts = sessions.size();
        long completedAttempts = sessions.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .count();

        IntSummaryStatistics stats = sessions.stream()
                .filter(s -> s.getScore() != null)
                .mapToInt(TestSession::getScore)
                .summaryStatistics();

        return Map.of(
                "testConfigId", testConfigId,
                "totalAttempts", totalAttempts,
                "completedAttempts", completedAttempts,
                "averageScore", stats.getAverage(),
                "maxScore", stats.getMax(),
                "minScore", stats.getMin()
        );
    }
}