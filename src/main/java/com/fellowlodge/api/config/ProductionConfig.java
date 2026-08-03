package com.fellowlodge.api.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Fails fast when a production profile ({@code prod} or {@code supabase}) is
 * active but required secrets are missing, instead of silently falling back to
 * committed defaults. Nothing here is instantiated outside production profiles.
 */
@Configuration
@Profile({"prod", "supabase"})
@Slf4j
public class ProductionConfig {

    @Value("${DB_URL:}")
    private String dbUrl;

    @Value("${DB_USERNAME:}")
    private String dbUsername;

    @Value("${DB_PASSWORD:}")
    private String dbPassword;

    @Value("${SUPABASE_DB_HOST:}")
    private String supabaseDbHost;

    @Value("${SUPABASE_DB_USERNAME:}")
    private String supabaseDbUsername;

    @Value("${SUPABASE_DB_PASSWORD:}")
    private String supabaseDbPassword;

    @PostConstruct
    public void validateRequiredSecrets() {
        boolean usesSupabaseEnv = isNotBlank(supabaseDbHost) || isNotBlank(supabaseDbUsername);
        if (usesSupabaseEnv) {
            if (isBlank(supabaseDbHost)
                    || isBlank(supabaseDbUsername)
                    || isBlank(supabaseDbPassword)) {
                throw new IllegalStateException(
                        "SUPABASE_DB_HOST, SUPABASE_DB_USERNAME and SUPABASE_DB_PASSWORD are all required "
                        + "when running against Supabase. See .env.supabase.local.example.");
            }
        } else {
            if (isBlank(dbUrl) || isBlank(dbUsername) || isBlank(dbPassword)) {
                throw new IllegalStateException(
                        "DB_URL, DB_USERNAME and DB_PASSWORD (or SUPABASE_DB_*) are required "
                        + "when running with a production profile.");
            }
        }
        log.info("Production profile validated: database connection configured, uploads enabled.");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isNotBlank(String value) {
        return !isBlank(value);
    }
}
