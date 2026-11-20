package com.jonassavas.spring_task_api;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class PostgresMigrationIntegrationTest {
    private final String url = System.getenv("TEST_DATABASE_URL");
    private final String username = System.getenv("TEST_DATABASE_USERNAME");
    private final String password = System.getenv("TEST_DATABASE_PASSWORD");

    @Test
    void migratesAnEmptySchema() throws Exception {
        withSchema(
                schema -> {
                    Flyway flyway = flyway(schema);
                    assertThat(flyway.migrate().migrationsExecuted).isEqualTo(2);
                    flyway.validate();
                    try (Connection connection = connection(schema);
                            Statement statement = connection.createStatement()) {
                        try (ResultSet result =
                                statement.executeQuery(
                                        "SELECT COUNT(*) FROM information_schema.columns "
                                                + "WHERE table_schema = '"
                                                + schema
                                                + "' AND column_name IN "
                                                + "('position', 'strike_through_tasks', 'sort_mode', 'due_date')")) {
                            result.next();
                            assertThat(result.getInt(1)).isEqualTo(6);
                        }
                    }
                });
    }

    @Test
    void upgradesLegacyAndPartiallyUpgradedSchemas() throws Exception {
        for (boolean partial : new boolean[] {false, true}) {
            withSchema(
                    schema -> {
                        try (Connection connection = connection(schema);
                                Statement statement = connection.createStatement()) {
                            statement.execute(
                                    "CREATE TABLE users (id BIGINT PRIMARY KEY, username VARCHAR(30) NOT NULL, "
                                            + "password VARCHAR(100) NOT NULL, email VARCHAR(254) NOT NULL)");
                            statement.execute(
                                    "CREATE TABLE task_boards (id BIGINT PRIMARY KEY, "
                                            + "task_board_name VARCHAR(50) NOT NULL, owner_id BIGINT NOT NULL)");
                            statement.execute(
                                    "CREATE TABLE task_groups (id BIGINT PRIMARY KEY, "
                                            + "task_group_name VARCHAR(50) NOT NULL, task_board_id BIGINT NOT NULL)");
                            statement.execute(
                                    "CREATE TABLE tasks (id BIGINT PRIMARY KEY, task_name VARCHAR(100) NOT NULL, "
                                            + "group_id BIGINT NOT NULL)");
                            if (partial) {
                                statement.execute(
                                        "ALTER TABLE task_groups ADD COLUMN position INTEGER NOT NULL DEFAULT 5");
                                statement.execute(
                                        "ALTER TABLE tasks ADD COLUMN position INTEGER NOT NULL DEFAULT 5");
                                statement.execute(
                                        "ALTER TABLE task_groups ADD COLUMN color VARCHAR(7)");
                                statement.execute(
                                        "ALTER TABLE tasks ADD CONSTRAINT old_task_position_unique "
                                                + "UNIQUE (group_id, position)");
                            }
                            statement.execute(
                                    "INSERT INTO users VALUES (1, 'legacy', 'password', 'legacy@example.com')");
                            statement.execute(
                                    "INSERT INTO task_boards VALUES (1, 'Board', 1), (2, 'Other', 1)");
                            statement.execute(
                                    partial
                                            ? "INSERT INTO task_groups (id, task_group_name, task_board_id, position) "
                                                    + "VALUES (1, 'One', 1, 5), (2, 'Two', 1, 9)"
                                            : "INSERT INTO task_groups VALUES (1, 'One', 1), (2, 'Two', 1)");
                            statement.execute(
                                    partial
                                            ? "INSERT INTO tasks (id, task_name, group_id, position) "
                                                    + "VALUES (1, 'First', 1, 5), (2, 'Second', 1, 9)"
                                            : "INSERT INTO tasks VALUES (1, 'First', 1), (2, 'Second', 1)");
                        }

                        Flyway flyway = flyway(schema);
                        flyway.baseline();
                        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);
                        flyway.validate();
                        try (Connection connection = connection(schema);
                                Statement statement = connection.createStatement()) {
                            assertThat(positions(statement, "task_boards")).containsExactly(0, 1);
                            assertThat(positions(statement, "task_groups")).containsExactly(0, 1);
                            assertThat(positions(statement, "tasks")).containsExactly(0, 1);
                            try (ResultSet result =
                                    statement.executeQuery(
                                            "SELECT strike_through_tasks, sort_mode FROM task_groups WHERE id = 1")) {
                                result.next();
                                assertThat(result.getBoolean(1)).isFalse();
                                assertThat(result.getString(2)).isEqualTo("MANUAL");
                            }
                        }
                    });
        }
    }

    private java.util.List<Integer> positions(Statement statement, String table) throws Exception {
        java.util.List<Integer> positions = new java.util.ArrayList<>();
        try (ResultSet result =
                statement.executeQuery("SELECT position FROM " + table + " ORDER BY id")) {
            while (result.next()) {
                positions.add(result.getInt(1));
            }
        }
        return positions;
    }

    private Flyway flyway(String schema) {
        return Flyway.configure()
                .dataSource(url, username, password)
                .schemas(schema)
                .defaultSchema(schema)
                .locations("classpath:db/migration")
                .baselineVersion("1")
                .load();
    }

    private Connection connection(String schema) throws Exception {
        Connection connection = DriverManager.getConnection(url, username, password);
        connection.createStatement().execute("SET search_path TO " + schema);
        return connection;
    }

    private void withSchema(SchemaTest test) throws Exception {
        String schema = "migration_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(url, username, password);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schema);
        }
        try {
            test.run(schema);
        } finally {
            try (Connection connection = DriverManager.getConnection(url, username, password);
                    Statement statement = connection.createStatement()) {
                statement.execute("DROP SCHEMA " + schema + " CASCADE");
            }
        }
    }

    private interface SchemaTest {
        void run(String schema) throws Exception;
    }
}
