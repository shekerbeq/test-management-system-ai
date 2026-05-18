package kz.testmanagement.aigenerator.repository;

import kz.testmanagement.core.entity.AiProviderSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiProviderSettingRepository extends JpaRepository<AiProviderSetting, Long> {
}
