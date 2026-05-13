package com.lifecyclehub.ui;

import com.lifecyclehub.database.DatabaseConnection;
import com.lifecyclehub.database.DatabaseInitializer;
import com.lifecyclehub.util.ThemeManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * MainApp — JavaFX Application entry point.
 * v1.2.0 — loads theme preference on startup.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   Project Life-Cycle Hub  v1.2.0    ║");
        System.out.println("╚══════════════════════════════════════╝");

        // Load saved theme preference before anything else
        ThemeManager.loadPreference();

        // Load custom app icon
        try {
            Image icon = new Image(
                    MainApp.class.getResourceAsStream("/icon.png"));
            primaryStage.getIcons().add(icon);
            System.out.println("[App] Icon loaded successfully.");
        } catch (Exception e) {
            System.err.println("[App] Icon not found: " + e.getMessage());
        }

        // Initialize database
        DatabaseInitializer.initialize();

        // Initialize window manager with stage
        WindowManager.initialize(primaryStage);

        // Show dashboard
        WindowManager.showDashboard();

        System.out.println("[App] Application started successfully.");
    }

    @Override
    public void stop() {
        System.out.println("[App] Application shutting down...");
        DatabaseConnection.closeConnection();
        System.out.println("[App] Goodbye.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}