package com.john.ecommerce.support;

import com.john.ecommerce.ECommerceApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Integration base: prefers Testcontainers when Docker is up; otherwise uses local PG/Redis
 * from {@code application-test.yml} (john-server ports).
 */
@SpringBootTest(classes = ECommerceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    protected static final boolean DOCKER = dockerAvailable();
    private static final AtomicBoolean SCHEMA_LOADED = new AtomicBoolean(false);

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES;
    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS;

    static {
        if (DOCKER) {
            POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("john-ecommerce")
                    .withUsername("john-ecommerce")
                    .withPassword("john-ecommerce-123");
            REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);
            POSTGRES.start();
            REDIS.start();
        } else {
            POSTGRES = null;
            REDIS = null;
        }
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        if (!DOCKER) {
            return;
        }
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.data.redis.database", () -> 0);

        if (SCHEMA_LOADED.compareAndSet(false, true)) {
            try {
                SqlScriptLoader.loadAll(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load SQL scripts into Testcontainers PG", e);
            }
        }
    }

    private static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }
}
