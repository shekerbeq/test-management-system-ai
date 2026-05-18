package kz.testmanagement.aigenerator.repository;

import kz.testmanagement.core.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
    List<PromptTemplate> findByOwnerIdOrSystemTemplateTrue(Long ownerId);
}
