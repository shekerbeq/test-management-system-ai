package kz.testmanagement.test.repository;

import kz.testmanagement.test.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTestConfig_Id(Long testConfigId);
}
