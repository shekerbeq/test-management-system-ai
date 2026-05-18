package kz.testmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "kz.testmanagement")
@EnableJpaAuditing
@EnableScheduling
public class TestManagementSystemAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestManagementSystemAiApplication.class, args);
    }
}
