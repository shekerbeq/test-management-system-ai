package kz.testmanagement.test.integration;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class PostgresContainerSmokeTest {

    @Test
    void startsPostgresContainer() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")) {
            postgres.start();
            assertTrue(postgres.isRunning());
        }
    }
}
