package kz.testmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "kz.testmanagement")
@EnableJpaAuditing
public class TestManagementSystemAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestManagementSystemAiApplication.class, args);
    }
}