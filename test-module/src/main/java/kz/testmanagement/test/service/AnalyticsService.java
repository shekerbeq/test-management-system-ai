package kz.testmanagement.test.service;

import kz.testmanagement.test.entity.TestSession;
import kz.testmanagement.test.repository.TestSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TestSessionRepository sessionRepository;

    public Map<String, Object> getTestStatistics(Long testConfigId) {
        List<TestSession> sessions = sessionRepository.findByTestConfig_Id(testConfigId);
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

    public List<Map<String, Object>> getRating(Long testConfigId) {
        return sessionRepository.findByTestConfig_Id(testConfigId).stream()
                .filter(session -> "COMPLETED".equals(session.getStatus()))
                .sorted(Comparator.comparing(TestSession::getScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(session -> {
                    Map<String, Object> row = new java.util.LinkedHashMap<>();
                    row.put("sessionId", session.getId());
                    row.put("studentId", session.getStudentId());
                    row.put("score", session.getScore() == null ? 0 : session.getScore());
                    row.put("finishedAt", session.getFinishedAt());
                    return row;
                })
                .toList();
    }
}
