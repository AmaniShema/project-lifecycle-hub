package com.lifecyclehub.ui;

import com.lifecyclehub.controller.ProjectDetailController;
import com.lifecyclehub.util.ThemeManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * WindowManager — centralizes all window and scene transitions.
 * v1.2.0 — integrated ThemeManager for dark/light theme support.
 *
 * Key design decision:
 *   We maintain ONE Scene object and swap only the root node.
 *   This means ThemeManager only needs to manage ONE scene's
 *   stylesheets — toggling theme instantly affects the whole app
 *   regardless of which screen is showing.
 */
public class WindowManager {

    // ─────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────

    private static final String APP_TITLE  = "Project Life-Cycle Hub";
    private static final int    MIN_WIDTH  = 900;
    private static final int    MIN_HEIGHT = 600;

    // ─────────────────────────────────────────
    // State
    // ─────────────────────────────────────────

    private static Stage primaryStage;

    /**
     * The single shared Scene.
     * We reuse this scene and just swap the root node on navigation.
     * This is the key to instant theme switching — ThemeManager
     * holds a reference to this one scene.
     */
    private static Scene sharedScene;

    // ─────────────────────────────────────────
    // Initialization
    // ─────────────────────────────────────────

    /**
     * Must be called once from MainApp.start() before any navigation.
     */
    public static void initialize(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setWidth(1100);
        primaryStage.setHeight(700);
        // sharedScene is created on first showDashboard() call
    }

    // ─────────────────────────────────────────
    // SHOW DASHBOARD
    // ─────────────────────────────────────────

    /**
     * Navigates to the main Dashboard screen.
     */
    public static void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    WindowManager.class.getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            if (sharedScene == null) {
                // First load — create the scene
                sharedScene = new Scene(root, 1100, 700);
                // Register with ThemeManager — this is the only call needed
                ThemeManager.setScene(sharedScene);
            } else {
                // Subsequent loads — just swap root, keep same scene
                sharedScene.setRoot(root);
                // Re-apply theme so new root picks up stylesheets
                ThemeManager.applyCurrentTheme();
            }

            primaryStage.setScene(sharedScene);
            primaryStage.setTitle(APP_TITLE + " — Dashboard");
            primaryStage.show();

            System.out.println("[WindowManager] Showing Dashboard.");

        } catch (IOException e) {
            System.err.println("[WindowManager] Failed to load dashboard: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────
    // SHOW PROJECT DETAIL
    // ─────────────────────────────────────────

    /**
     * Opens the Project Detail screen for a specific project.
     *
     * @param projectId the ID of the project to display
     */
    public static void showProjectDetail(int projectId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    WindowManager.class.getResource(
                            "/fxml/project_detail.fxml"));
            Parent root = loader.load();

            // Pass the project ID to the controller
            ProjectDetailController controller = loader.getController();
            controller.initData(projectId);

            if (sharedScene == null) {
                sharedScene = new Scene(root, 1100, 700);
                ThemeManager.setScene(sharedScene);
            } else {
                sharedScene.setRoot(root);
                ThemeManager.applyCurrentTheme();
            }

            primaryStage.setScene(sharedScene);
            primaryStage.setTitle(APP_TITLE + " — Project Detail");
            primaryStage.show();

            System.out.println("[WindowManager] Showing ProjectDetail id="
                    + projectId);

        } catch (IOException e) {
            System.err.println("[WindowManager] Failed to load project detail: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────

    public static Stage getPrimaryStage() { return primaryStage; }
    public static Scene getSharedScene()  { return sharedScene; }
}