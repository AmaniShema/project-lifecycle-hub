package com.lifecyclehub.ui;

/**
 * Launcher — application entry point for packaged executable.
 *
 * WHY THIS CLASS EXISTS:
 *
 * When a class that extends javafx.application.Application is used
 * as the main class directly in a fat JAR or jpackage bundle,
 * the JavaFX runtime performs a module system check and fails with:
 * "JavaFX runtime components are missing"
 *
 * The fix: use a plain Java class (no JavaFX imports, no extends)
 * as the true entry point. This bypasses the module check entirely.
 * This class simply delegates to MainApp.main().
 *
 * This is the standard pattern for all packaged JavaFX applications.
 */
public class Launcher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
