package com.lifecyclehub.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseConnection — Singleton pattern.
 *
 * Manages a single shared SQLite connection for the entire application.
 * SQLite is file-based: the database is stored at:
 *     ~/lifecyclehub.db
 *
 * Why Singleton?
 * SQLite performs best with one open connection.
 * Multiple connections to the same file can cause locking errors.
 */
public class DatabaseConnection {

    // ─────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────

    /**
     * Path to the SQLite database file.
     * Stored in the user's home directory for easy access.
     */
    private static final String DB_URL =
            "jdbc:sqlite:" + System.getProperty("user.home") + "/lifecyclehub.db";

    // ─────────────────────────────────────────
    // Singleton Instance
    // ─────────────────────────────────────────

    /** The single shared connection instance. */
    private static Connection instance = null;

    // ─────────────────────────────────────────
    // Private Constructor — prevents instantiation
    // ─────────────────────────────────────────

    private DatabaseConnection() {}

    // ─────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────

    /**
     * Returns the shared SQLite connection.
     * Creates it on first call (lazy initialization).
     *
     * @return the active Connection object
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {

        if (instance == null || instance.isClosed()) {
            // Create the connection to the SQLite file
            instance = DriverManager.getConnection(DB_URL);

            // CRITICAL: Enable foreign key enforcement
            // SQLite disables foreign keys by default — we must enable them
            // every time a new connection is opened.
            try (Statement stmt = instance.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            System.out.println("[DB] Connected to: " + DB_URL);
        }

        return instance;
    }

    /**
     * Closes the database connection cleanly.
     * Call this when the application is shutting down.
     */
    public static void closeConnection() {
        if (instance != null) {
            try {
                if (!instance.isClosed()) {
                    instance.close();
                    System.out.println("[DB] Connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            } finally {
                instance = null;
            }
        }
    }

    /**
     * Returns the file path of the database for display purposes.
     */
    public static String getDatabasePath() {
        return DB_URL.replace("jdbc:sqlite:", "");
    }
}