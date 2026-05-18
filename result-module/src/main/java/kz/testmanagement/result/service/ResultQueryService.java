package kz.testmanagement.result.service;

import kz.testmanagement.test.repository.TestSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResultQueryService {

    private final TestSessionRepository testSessionRepository;

    public Map<String, Object> getSessionResult(Long sessionId) {
        var session = testSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        int total = session.getTestConfig() == null ? 0 : session.getTestConfig().getCount();
        int score = session.getScore() == null ? 0 : session.getScore();
        double percentage = total == 0 ? 0 : (score * 100.0 / total);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", session.getId());
        result.put("testId", session.getTestConfig() == null ? null : session.getTestConfig().getId());
        result.put("creatorId", session.getTestConfig() == null ? null : session.getTestConfig().getCreatorId());
        result.put("studentId", session.getStudentId());
        result.put("status", session.getStatus());
        result.put("score", score);
        result.put("total", total);
        result.put("percentage", percentage);
        result.put("passed", percentage >= 50);
        result.put("startedAt", session.getStartedAt());
        result.put("finishedAt", session.getFinishedAt());
        return result;
    }
}
