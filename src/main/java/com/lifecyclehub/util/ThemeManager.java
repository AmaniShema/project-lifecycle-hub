package com.lifecyclehub.util;

import javafx.scene.Scene;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * ThemeManager — handles dark/light theme switching.
 * Dark theme: obsidian.css   (replaces old dashboard.css)
 * Light theme: light-theme.css
 * Preference saved to: ~/.lifecyclehub.properties
 */
public class ThemeManager {

    public enum Theme { DARK, LIGHT }

    // ── CSS Paths (classpath resources) ─────────────────────
    private static final String DARK_CSS  = "/css/obsidian.css";
    private static final String LIGHT_CSS = "/css/light-theme.css";

    // ── Preferences File ─────────────────────────────────────
    private static final String PREFS_FILE = System.getProperty("user.home")
            + "/.lifecyclehub.properties";
    private static final String PREFS_KEY  = "theme";

    // ── State ─────────────────────────────────────────────────
    private static Theme  currentTheme = Theme.DARK;
    private static Scene  sharedScene  = null;

    // ─────────────────────────────────────────────────────────

    public static void setScene(Scene scene) {
        sharedScene = scene;
    }

    public static void loadPreference() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(PREFS_FILE)) {
            props.load(fis);
            String saved = props.getProperty(PREFS_KEY, "DARK");
            currentTheme = "LIGHT".equals(saved) ? Theme.LIGHT : Theme.DARK;
        } catch (IOException e) {
            currentTheme = Theme.DARK; // default
        }
        System.out.println("[ThemeManager] Loaded theme: " + currentTheme);
    }

    public static void savePreference() {
        Properties props = new Properties();
        props.setProperty(PREFS_KEY, currentTheme.name());
        try (FileOutputStream fos = new FileOutputStream(PREFS_FILE)) {
            props.store(fos, "Project Life-Cycle Hub preferences");
        } catch (IOException e) {
            System.err.println("[ThemeManager] Could not save preference: " + e.getMessage());
        }
    }

    public static void toggle() {
        currentTheme = (currentTheme == Theme.DARK) ? Theme.LIGHT : Theme.DARK;
        savePreference();
        System.out.println("[ThemeManager] Toggled to: " + currentTheme);
    }

    public static void applyCurrentTheme() {
        if (sharedScene == null) {
            System.err.println("[ThemeManager] No scene set — cannot apply theme.");
            return;
        }

        sharedScene.getStylesheets().clear();
        String cssPath = currentTheme == Theme.DARK ? DARK_CSS : LIGHT_CSS;

        java.net.URL cssUrl = ThemeManager.class.getResource(cssPath);
        if (cssUrl != null) {
            sharedScene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("[ThemeManager] Applied theme: " + currentTheme);
        } else {
            System.err.println("[ThemeManager] CSS not found: " + cssPath);
        }
    }

    public static String getToggleLabel() {
        return currentTheme == Theme.DARK ? "☀ Light" : "🌙 Dark";
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }
}