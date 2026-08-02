package com.john.ecommerce.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt = new Jwt();
    private Auth auth = new Auth();
    private Mail mail = new Mail();
    private Minio minio = new Minio();
    private Cors cors = new Cors();
    private Trade trade = new Trade();

    @Data
    public static class Jwt {
        private String secret = "john-ecommerce-dev-jwt-secret-change-me-32b+";
        private long expireMs = 604800000L; // 7 days
    }

    @Data
    public static class Auth {
        private long codeTtlSeconds = 300L;
        /** 非空时固定验证码（dev/CI）；生产请置空 */
        private String fixedCode = "";
    }

    /**
     * SMTP 配置，环境变量与 john-ip-studio 对齐：
     * SMTP_HOST / SMTP_PORT / SMTP_USER / SMTP_PASSWORD / SMTP_FROM / SMTP_USE_TLS / SMTP_USE_SSL
     */
    @Data
    public static class Mail {
        private String host = "";
        private int port = 465;
        private String username = "";
        private String password = "";
        private String from = "";
        private boolean useTls = false;
        private boolean useSsl = true;
    }

    @Data
    public static class Minio {
        private String endpoint = "http://127.0.0.1:19000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String bucket = "john-ecommerce";
        /** Browser-reachable base URL for uploaded objects (defaults to endpoint). */
        private String publicBaseUrl = "";
    }

    @Data
    public static class Cors {
        private String origins = "http://localhost:3022,http://localhost:3021";
    }

    @Data
    public static class Trade {
        /** Unpaid order auto-cancel window (ms). Default 30 minutes. */
        private long payTimeoutMs = 30L * 60 * 1000;
        /** Delay between unpaid-cancel job runs (ms). */
        private long unpaidCancelDelayMs = 60_000L;
    }
}
