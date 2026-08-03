package com.fellowlodge.api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Map;

@Component
@Profile("supabase")
@RequiredArgsConstructor
@Slf4j
public class SupabaseConnectivityProbe implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final Flyway flyway;

    @Override
    public void run(ApplicationArguments args) {
        Connection connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        try {
            DatabaseMetaData meta = connection.getMetaData();
            log.info("Connected to Supabase PostgreSQL: {} {} / {}", meta.getDatabaseProductName(),
                    meta.getDatabaseProductVersion(), meta.getURL());
            log.info("PostgreSQL driver: {} {}", meta.getDriverName(), meta.getDriverVersion());
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException("Failed to inspect Supabase PostgreSQL metadata", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
        }

        var info = flyway.info();
        log.info("Flyway schema version: {} | applied migrations: {}", info.current().getVersion(),
                info.applied().length);

        List<Map<String, Object>> migrations = jdbcTemplate.queryForList(
                "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank");
        migrations.forEach(row -> log.info("Flyway migration {} - {} (success={})",
                row.get("version"), row.get("description"), row.get("success")));
    }
}
