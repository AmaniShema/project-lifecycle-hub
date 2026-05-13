package com.lifecyclehub.service;

import com.lifecyclehub.repository.TaskRepository;

/**
 * ProgressService — calculates project progress dynamically.
 *
 * ╔══════════════════════════════════════════════════╗
 * ║  CORE RULE: Progress is NEVER stored in the DB.  ║
 * ║  It is ALWAYS calculated fresh from task data.   ║
 * ╚══════════════════════════════════════════════════╝
 *
 * Formula:
 *   progress = (completedTasks / totalTasks) * 100
 *
 * Edge cases handled:
 *   - 0 tasks → 0% progress (not a division by zero)
 *   - All tasks done → 100%
 *   - Result is always 0–100 (clamped)
 */
public class ProgressService {

    // ─────────────────────────────────────────
    // Dependencies
    // ─────────────────────────────────────────

    private final TaskRepository taskRepository;

    // ─────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────

    public ProgressService() {
        this.taskRepository = new TaskRepository();
    }

    // ─────────────────────────────────────────
    // CALCULATE PROGRESS
    // ─────────────────────────────────────────

    /**
     * Calculates the completion percentage for a project.
     *
     * @param projectId the project ID
     * @return progress as a double between 0.0 and 100.0
     */
    public double calculateProgress(int projectId) {
        int total     = taskRepository.countTotal(projectId);
        int completed = taskRepository.countCompleted(projectId);

        // Guard: avoid division by zero when no tasks exist
        if (total == 0) {
            return 0.0;
        }

        double progress = ((double) completed / (double) total) * 100.0;

        // Clamp between 0 and 100 (defensive coding)
        progress = Math.max(0.0, Math.min(100.0, progress));

        System.out.println("[ProgressService] Project id=" + projectId
                + " → " + completed + "/" + total
                + " tasks = " + String.format("%.1f", progress) + "%");

        return progress;
    }

    // ─────────────────────────────────────────
    // PROGRESS AS INTEGER (for display)
    // ─────────────────────────────────────────

    /**
     * Returns progress as an integer percentage (0–100).
     * Useful for displaying "75%" labels in the UI.
     *
     * @param projectId the project ID
     * @return integer progress percentage
     */
    public int calculateProgressInt(int projectId) {
        return (int) Math.round(calculateProgress(projectId));
    }

    // ─────────────────────────────────────────
    // PROGRESS AS 0.0–1.0 (for JavaFX ProgressBar)
    // ─────────────────────────────────────────

    /**
     * Returns progress as a value between 0.0 and 1.0.
     * JavaFX ProgressBar expects this range natively.
     *
     * @param projectId the project ID
     * @return progress fraction for JavaFX ProgressBar
     */
    public double calculateProgressFraction(int projectId) {
        return calculateProgress(projectId) / 100.0;
    }

    // ─────────────────────────────────────────
    // TASK SUMMARY STRING
    // ─────────────────────────────────────────

    /**
     * Returns a human-readable task summary string.
     * Example: "3 / 7 tasks completed"
     *
     * @param projectId the project ID
     * @return formatted summary string
     */
    public String getTaskSummary(int projectId) {
        int total     = taskRepository.countTotal(projectId);
        int completed = taskRepository.countCompleted(projectId);
        return completed + " / " + total + " tasks completed";
    }
}