package com.fellowlodge.api.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

class SqliteToPostgresMigratorTest {

    @TempDir
    Path tempDir;

    private Path createLegacyDatabase() throws Exception {
        Path db = tempDir.resolve("fellowlodge.db");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + db)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE roles (id TEXT PRIMARY KEY, name TEXT UNIQUE NOT NULL, description TEXT)");
                stmt.execute("CREATE TABLE guests (id TEXT PRIMARY KEY, first_name TEXT NOT NULL, last_name TEXT NOT NULL, "
                        + "email TEXT, is_vip INTEGER DEFAULT 0, phone TEXT)");
                stmt.execute("CREATE TABLE rooms (id TEXT PRIMARY KEY, room_number TEXT UNIQUE NOT NULL, "
                        + "price_per_night REAL NOT NULL, status TEXT)");
                stmt.execute("INSERT INTO roles VALUES ('a1', 'Admin', 'Has access')");
                stmt.execute("INSERT INTO roles VALUES ('a2', 'Guest', NULL)");
                stmt.execute("INSERT INTO guests VALUES ('g1', 'Jane', 'O''Brien', 'jane@example.com', 1, '+1 555')");
                stmt.execute("INSERT INTO rooms VALUES ('r1', '101', 120.5, 'Available')");
            }
        }
        return db;
    }

    @Test
    @DisplayName("Generates FK-ordered INSERT statements with correct escaping and NULL handling")
    void generatesValidMigrationScript() throws Exception {
        Path db = createLegacyDatabase();

        String sql = SqliteToPostgresMigrator.generate(db, SqliteToPostgresMigrator.TABLE_ORDER);

        assertThat(sql).contains("BEGIN;");
        assertThat(sql).endsWith("COMMIT;\n");

        int roles = sql.indexOf("INSERT INTO \"roles\"");
        int guests = sql.indexOf("INSERT INTO \"guests\"");
        int rooms = sql.indexOf("INSERT INTO \"rooms\"");

        assertThat(roles).isGreaterThan(-1);
        assertThat(guests).isGreaterThan(roles);
        assertThat(rooms).isGreaterThan(guests);

        assertThat(sql).contains("INSERT INTO \"roles\" (\"id\", \"name\", \"description\") VALUES ('a1', 'Admin', 'Has access');");
        assertThat(sql).contains("INSERT INTO \"roles\" (\"id\", \"name\", \"description\") VALUES ('a2', 'Guest', NULL);");
        assertThat(sql).contains("INSERT INTO \"guests\" (\"id\", \"first_name\", \"last_name\", \"email\", \"is_vip\", \"phone\") "
                + "VALUES ('g1', 'Jane', 'O''Brien', 'jane@example.com', 1, '+1 555');");
        assertThat(sql).contains("INSERT INTO \"rooms\" (\"id\", \"room_number\", \"price_per_night\", \"status\") "
                + "VALUES ('r1', '101', 120.5, 'Available');");
    }

    @Test
    @DisplayName("Skips tables that do not exist in the source database")
    void skipsMissingTables() throws Exception {
        Path db = createLegacyDatabase();

        String sql = SqliteToPostgresMigrator.generate(db, java.util.List.of("guests", "does_not_exist"));

        assertThat(sql).contains("INSERT INTO \"guests\"");
        assertThat(sql).doesNotContain("does_not_exist");
    }
}
