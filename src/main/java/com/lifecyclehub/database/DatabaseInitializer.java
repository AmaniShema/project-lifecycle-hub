package com.lifecyclehub.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseInitializer — runs on every application startup.
 * v2.2.0 — added tasks.notes migration.
 */
public class DatabaseInitializer {

    private static final String CREATE_PROJECTS_TABLE = """
            CREATE TABLE IF NOT EXISTS projects (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                name          TEXT    NOT NULL,
                description   TEXT    DEFAULT '',
                notes         TEXT    DEFAULT '',
                created_at    TEXT    NOT NULL,
                current_stage TEXT    NOT NULL DEFAULT 'IDEA',
                archived      INTEGER DEFAULT 0
            );
            """;

    private static final String CREATE_STAGES_TABLE = """
            CREATE TABLE IF NOT EXISTS stages (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                name       TEXT    NOT NULL,
                project_id INTEGER NOT NULL,
                notes      TEXT    DEFAULT '',
                FOREIGN KEY (project_id)
                    REFERENCES projects(id)
                    ON DELETE CASCADE
            );
            """;

    private static final String CREATE_TASKS_TABLE = """
            CREATE TABLE IF NOT EXISTS tasks (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                title      TEXT    NOT NULL,
                completed  INTEGER NOT NULL DEFAULT 0,
                created_at TEXT    NOT NULL,
                stage_id   INTEGER NOT NULL,
                project_id INTEGER NOT NULL,
                due_date   TEXT    DEFAULT NULL,
                priority   TEXT    DEFAULT 'NONE',
                notes      TEXT    DEFAULT '',
                FOREIGN KEY (stage_id)
                    REFERENCES stages(id)
                    ON DELETE CASCADE,
                FOREIGN KEY (project_id)
                    REFERENCES projects(id)
                    ON DELETE CASCADE
            );
            """;

    public static void initialize() {
        System.out.println("[DB] Initializing database schema...");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(CREATE_PROJECTS_TABLE);
            System.out.println("[DB] Table 'projects' ready.");
            stmt.execute(CREATE_STAGES_TABLE);
            System.out.println("[DB] Table 'stages' ready.");
            stmt.execute(CREATE_TASKS_TABLE);
            System.out.println("[DB] Table 'tasks' ready.");

            runMigrations(conn);

            System.out.println("[DB] Schema initialization complete.");
            System.out.println("[DB] Database file: " + DatabaseConnection.getDatabasePath());

        } catch (SQLException e) {
            System.err.println("[DB] FATAL: Schema initialization failed!");
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    private static void runMigrations(Connection conn) throws SQLException {
        addColumnIfMissing(conn, "projects", "notes",    "TEXT DEFAULT ''");
        addColumnIfMissing(conn, "stages",   "notes",    "TEXT DEFAULT ''");
        addColumnIfMissing(conn, "tasks",    "due_date", "TEXT DEFAULT NULL");
        addColumnIfMissing(conn, "projects", "archived", "INTEGER DEFAULT 0");
        addColumnIfMissing(conn, "tasks",    "priority", "TEXT DEFAULT 'NONE'");
        addColumnIfMissing(conn, "tasks",    "notes",    "TEXT DEFAULT ''");
    }

    private static void addColumnIfMissing(Connection conn,
            String table, String column, String definition) throws SQLException {

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    System.out.println("[DB] Column '" + column
                        + "' already exists in '" + table + "'. Skipping.");
                    return;
                }
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + table
                + " ADD COLUMN " + column + " " + definition);
            System.out.println("[DB] Migration: added column '"
                + column + "' to '" + table + "'.");
        }
    }
}
