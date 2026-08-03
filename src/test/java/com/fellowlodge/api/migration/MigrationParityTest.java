package com.fellowlodge.api.migration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Automated parity audit between the legacy SQLite schema (FELLOW-LODGE
 * schema.sql) and the centralized PostgreSQL Flyway migrations (V1..V3).
 * Guarantees every legacy table, column, index and CHECK constraint is
 * preserved in PostgreSQL.
 */
class MigrationParityTest {

    private static final Pattern TABLE_BLOCK = Pattern.compile(
            "CREATE TABLE IF NOT EXISTS (\\w+) \\(([\\s\\S]*?)\\);");
    private static final Pattern INDEX = Pattern.compile(
            "CREATE (?:UNIQUE )?INDEX IF NOT EXISTS (\\w+)\\s+ON\\s+(\\w+)\\(([^)]*)\\)");
    private static final Pattern COLUMN = Pattern.compile(
            "^\\s{4}\"?(\\w+)\"?\\s+", Pattern.MULTILINE);
    private static final Pattern CHECK = Pattern.compile(
            "CHECK \\([\\w\"']+ IN \\(([^)]*)\\)\\)");

    private final String legacy = readFile(
            Path.of("..", "FELLOW-LODGE", "src", "main", "resources", "database", "schema.sql"));
    private final String postgres = read("db/migration/V1__init.sql")
            + "\n" + read("db/migration/V2__seed_data.sql")
            + "\n" + read("db/migration/V3__migration_parity.sql");

    private String readFile(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }

    private String read(String path) {
        try {
            return new String(new ClassPathResource(path).getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }

    private Map<String, String> parseTables(String sql) {
        Map<String, String> tables = new HashMap<>();
        Matcher m = TABLE_BLOCK.matcher(sql);
        while (m.find()) {
            tables.put(m.group(1), m.group(2));
        }
        return tables;
    }

    private Set<String> parseColumns(String tableBody) {
        Set<String> columns = new HashSet<>();
        Matcher m = COLUMN.matcher(tableBody);
        while (m.find()) {
            columns.add(m.group(1));
        }
        columns.remove("CONSTRAINT");
        columns.remove("PRIMARY");
        columns.remove("FOREIGN");
        columns.remove("UNIQUE");
        return columns;
    }

    private Set<String> parseChecks(String tableBody) {
        Set<String> checks = new HashSet<>();
        Matcher m = CHECK.matcher(tableBody);
        while (m.find()) {
            checks.add(m.group(1).replaceAll("\\s+", ""));
        }
        return checks;
    }

    @Test
    @DisplayName("Every legacy SQLite table is present in the PostgreSQL schema")
    void allLegacyTablesPreserved() {
        Set<String> legacyTables = parseTables(legacy).keySet();
        Set<String> postgresTables = parseTables(postgres).keySet();
        assertThat(postgresTables).containsAll(legacyTables);
    }

    @Test
    @DisplayName("Every legacy SQLite column is preserved in the PostgreSQL schema")
    void allLegacyColumnsPreserved() {
        Map<String, String> legacyTables = parseTables(legacy);
        Map<String, String> postgresTables = parseTables(postgres);
        for (Map.Entry<String, String> entry : legacyTables.entrySet()) {
            Set<String> legacyColumns = parseColumns(entry.getValue());
            Set<String> postgresColumns = parseColumns(postgresTables.get(entry.getKey()));
            assertThat(postgresColumns)
                    .as("columns of legacy table %s", entry.getKey())
                    .containsAll(legacyColumns);
        }
    }

    @Test
    @DisplayName("Every legacy SQLite index is preserved in the PostgreSQL schema")
    void allLegacyIndexesPreserved() {
        Set<String> legacyIndexes = new HashSet<>();
        Matcher m = INDEX.matcher(legacy);
        while (m.find()) {
            legacyIndexes.add(m.group(1) + ":" + m.group(2) + "(" + m.group(3).replaceAll("\\s+", "") + ")");
        }

        Set<String> postgresIndexes = new HashSet<>();
        m = INDEX.matcher(postgres);
        while (m.find()) {
            postgresIndexes.add(m.group(1) + ":" + m.group(2) + "(" + m.group(3).replaceAll("\\s+", "") + ")");
        }

        assertThat(postgresIndexes).containsAll(legacyIndexes);
    }

    @Test
    @DisplayName("Every legacy SQLite CHECK constraint value list is preserved in the PostgreSQL schema")
    void allLegacyChecksPreserved() {
        Map<String, String> legacyTables = parseTables(legacy);
        Map<String, String> postgresTables = parseTables(postgres);
        for (Map.Entry<String, String> entry : legacyTables.entrySet()) {
            Set<String> legacyChecks = parseChecks(entry.getValue());
            Set<String> postgresChecks = parseChecks(postgresTables.get(entry.getKey()));
            assertThat(postgresChecks)
                    .as("CHECK constraints of legacy table %s", entry.getKey())
                    .containsAll(legacyChecks);
        }
    }

    @Test
    @DisplayName("Migration report lists every legacy table")
    void migrationReportCoversAllTables() throws IOException {
        String report = Files.readString(Path.of("..", "docs", "migration-report.md"));
        List<String> reportLines = new ArrayList<>(List.of(report.split("\\R")));
        Set<String> legacyTables = parseTables(legacy).keySet();
        for (String table : legacyTables) {
            assertThat(reportLines)
                    .as("migration report must document table %s", table)
                    .anyMatch(line -> line.contains("`" + table + "`"));
        }
    }
}
