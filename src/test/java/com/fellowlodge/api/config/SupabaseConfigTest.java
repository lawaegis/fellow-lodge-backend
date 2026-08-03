package com.fellowlodge.api.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupabaseConfigTest {

    private static final YamlPropertySourceLoader YAML = new YamlPropertySourceLoader();
    private static Map<String, Object> appConfig;

    @BeforeAll
    static void loadConfig() throws IOException {
        appConfig = new HashMap<>();
        for (String file : List.of("application.yml", "application-supabase.yml")) {
            YAML.load(file.replace(".yml", ""), new ClassPathResource(file))
                    .forEach(source -> appConfig.putAll((Map<String, Object>) source.getSource()));
        }
    }

    private String resolve(String property, Map<String, String> env) {
        MutablePropertySources sources = new MutablePropertySources();
        sources.addLast(new MapPropertySource("env", new HashMap<>(env)));
        sources.addLast(new MapPropertySource("config", appConfig));
        PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(sources);
        return resolver.resolveRequiredPlaceholders(String.valueOf(appConfig.get(property)));
    }

    private Map<String, String> supabaseEnv() {
        return Map.of(
                "SUPABASE_DB_HOST", "db.abcdefghij.supabase.co",
                "SUPABASE_DB_PORT", "5432",
                "SUPABASE_DB_NAME", "postgres",
                "SUPABASE_DB_USERNAME", "postgres",
                "SUPABASE_DB_PASSWORD", "test-password");
    }

    @Test
    @DisplayName("SUPABASE_DB_* environment variables substitute into the PostgreSQL datasource")
    void envVarsResolveIntoDatasource() {
        assertThat(resolve("spring.datasource.url", supabaseEnv()))
                .isEqualTo("jdbc:postgresql://db.abcdefghij.supabase.co:5432/postgres?sslmode=require&prepareThreshold=0");
        assertThat(resolve("spring.datasource.username", supabaseEnv())).isEqualTo("postgres");
        assertThat(resolve("spring.datasource.password", supabaseEnv())).isEqualTo("test-password");
        assertThat(resolve("spring.datasource.driver-class-name", supabaseEnv()))
                .isEqualTo("org.postgresql.Driver");
    }

    @Test
    @DisplayName("Missing SUPABASE_DB_* environment variables fail fast at startup")
    void missingEnvVarsFailFast() {
        Map<String, String> empty = Map.of();
        assertThatThrownBy(() -> resolve("spring.datasource.url", empty))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SUPABASE_DB_HOST");
        assertThatThrownBy(() -> resolve("spring.datasource.username", empty))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SUPABASE_DB_USERNAME");
    }

    @Test
    @DisplayName("Supabase profile tunes Hikari pool, validates schema and enables Flyway on PostgreSQL")
    void poolAndFlywayConfigured() {
        assertThat(String.valueOf(appConfig.get("spring.datasource.hikari.pool-name")))
                .isEqualTo("FellowLodgeSupabasePool");
        assertThat(String.valueOf(appConfig.get("spring.datasource.hikari.maximum-pool-size")))
                .isEqualTo("${SUPABASE_DB_POOL_SIZE:10}");
        assertThat(String.valueOf(appConfig.get("spring.flyway.enabled"))).isEqualTo("true");
        assertThat(String.valueOf(appConfig.get("spring.jpa.hibernate.ddl-auto"))).isEqualTo("validate");
        assertThat(String.valueOf(appConfig.get("spring.jpa.database-platform")))
                .isEqualTo("org.hibernate.dialect.PostgreSQLDialect");
    }
}
