package com.john.ecommerce.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt = new Jwt();
    private Minio minio = new Minio();
    private Cors cors = new Cors();

    @Data
    public static class Jwt {
        private String secret = "change-me-in-production";
        private long expireMs = 604800000L; // 7 days
    }

    @Data
    public static class Minio {
        private String endpoint = "http://john-server:19000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String bucket = "john-ecommerce";
    }

    @Data
    public static class Cors {
        private String origins = "http://localhost:3020,http://localhost:3021";
    }
}
