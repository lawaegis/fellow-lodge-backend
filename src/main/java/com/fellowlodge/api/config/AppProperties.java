package com.fellowlodge.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Storage storage = new Storage();
    private final Security security = new Security();
    private final Cors cors = new Cors();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs;
        private long refreshTokenExpirationMs;
    }

    @Getter
    @Setter
    public static class Storage {
        private String uploadDir;
    }

    @Getter
    @Setter
    public static class Security {
        private int maxLoginAttempts;
        private int lockDurationMinutes;
    }

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigins;
    }
}
