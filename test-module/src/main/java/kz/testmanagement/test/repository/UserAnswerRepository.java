package kz.testmanagement.test.repository;

import kz.testmanagement.test.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    List<UserAnswer> findByTestSession_Id(Long sessionId);
    void deleteByTestSession_Id(Long sessionId);
    void deleteByQuestion_Id(Long questionId);
}
