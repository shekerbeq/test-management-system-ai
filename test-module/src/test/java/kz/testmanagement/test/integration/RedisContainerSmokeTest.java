package kz.testmanagement.test.integration;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class RedisContainerSmokeTest {

    @Test
    void startsRedisContainer() {
        try (GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379)) {
            redis.start();
            assertTrue(redis.isRunning());
        }
    }
}
